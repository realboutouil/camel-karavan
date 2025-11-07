/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.karavan.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.karavan.config.KaravanProperties;
import org.apache.camel.karavan.model.*;
import org.apache.camel.karavan.service.CodeService;
import org.apache.camel.karavan.service.ConfigService;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.camel.karavan.KaravanConstants.LABEL_PROJECT_ID;
import static org.apache.camel.karavan.KaravanConstants.LABEL_TYPE;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DockerService {

    private final KaravanProperties properties;
    private final DockerEventHandler dockerEventHandler;
    private final CodeService codeService;
    private final Vertx vertx;
    private volatile DockerClient dockerClient;
    private volatile DockerClient dockerClientConnectedToRegistry;

    void onStart(@Observes StartupEvent ev) {
        if (!ConfigService.inKubernetes()) {
            try (EventsCmd cmd = getDockerClient().eventsCmd()) {
                cmd.exec(dockerEventHandler);
            }
        }
    }

    public DockerClient getDockerClient() {
        if (dockerClient == null) {
            synchronized (this) {
                if (dockerClient == null) {
                    DockerClientConfig config = getDockerClientConfig(true);
                    DockerHttpClient httpClient = getDockerHttpClient(config);
                    dockerClient = DockerClientImpl.getInstance(config, httpClient);
                }
            }
        }
        return dockerClient;
    }

    private DockerClientConfig getDockerClientConfig(boolean connectedToRegistry) {
        log.info("Docker Client Configuring " + (connectedToRegistry ? "( connectedToRegistry)" : ""));
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        if (connectedToRegistry) {
            log.info("Docker Client Registry " + properties.containerImage().registry());
            log.info("Docker Client Username " + (properties.containerImage().registryUsername().isPresent() ? "is not empty " : "is empty"));
            log.info("Docker Client Password " + (properties.containerImage().registryPassword().isPresent() ? "is not empty " : "is empty"));
            if (!Objects.equals(properties.containerImage().registry(), "registry:5000") && properties.containerImage().registryUsername().isPresent() && properties.containerImage().registryPassword().isPresent()) {
                builder.withRegistryUrl(properties.containerImage().registry());
                builder.withRegistryUsername(properties.containerImage().registryUsername().get());
                builder.withRegistryPassword(properties.containerImage().registryPassword().get());
            }
        }
        return builder.build();
    }

    private DockerHttpClient getDockerHttpClient(DockerClientConfig config) {
        return new ZerodepDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .build();
    }

    void onStop(@Observes ShutdownEvent ev) throws IOException {
        if (!ConfigService.inKubernetes()) {
            dockerEventHandler.close();
        }
    }

    public boolean checkDocker() {
        try {
            try (PingCmd cmd = getDockerClient().pingCmd()) {
                cmd.exec();
            }
            log.info("Docker is available");
            return true;
        } catch (Exception e) {
            log.error("Error connecting Docker: " + e.getMessage());
            return false;
        }
    }

    public Info getInfo() {
        try (InfoCmd cmd = getDockerClient().infoCmd()) {
            return cmd.exec();
        }
    }

    public Container getContainer(String id) {
        try (ListContainersCmd cmd = getDockerClient().listContainersCmd().withShowAll(true).withIdFilter(List.of(id))) {
            List<Container> containers = cmd.exec();
            return containers.isEmpty() ? null : containers.get(0);
        }
    }

    public Container getContainerByName(String name) {
        List<Container> containers = findContainer(name);
        return !containers.isEmpty() ? containers.get(0) : null;
    }

    public List<Container> getAllContainers() {
        try (ListContainersCmd cmd = getDockerClient().listContainersCmd().withShowAll(true)) {
            return cmd.exec();
        }
    }

    public Container createContainerFromCompose(DockerComposeService compose, Map<String, String> labels, PULL_IMAGE pullImage) throws InterruptedException {
        List<Container> containers = findContainer(compose.getContainer_name());
        if (containers.isEmpty()) {
            HealthCheck healthCheck = DockerUtils.getHealthCheck(compose.getHealthcheck());

            List<String> env = compose.getEnvironmentList();

            log.info("Compose Service started for {} in network:{}", compose.getContainer_name(), properties.docker().network());

            RestartPolicy restartPolicy = RestartPolicy.noRestart();
            if (Objects.equals(compose.getRestart(), RestartPolicy.onFailureRestart(10).getName())) {
                restartPolicy = RestartPolicy.onFailureRestart(10);
            } else if (Objects.equals(compose.getRestart(), RestartPolicy.alwaysRestart().getName())) {
                restartPolicy = RestartPolicy.alwaysRestart();
            }

            return createContainer(compose.getContainer_name(), compose.getImage(),
                    env, compose.getPortsMap(), healthCheck, labels, compose.getVolumes(), properties.docker().network(), restartPolicy, pullImage,
                    compose.getCpus(), compose.getCpu_percent(), compose.getMem_limit(), compose.getMem_reservation(), compose.getCommand());

        } else {
            log.info("Compose Service already exists: " + containers.get(0).getId());
            return containers.get(0);
        }
    }

    public List<Container> findContainer(String containerName) {
        try (ListContainersCmd cmd = getDockerClient().listContainersCmd().withShowAll(true).withNameFilter(List.of(containerName))) {
            return cmd.exec().stream().filter(c -> Objects.equals(c.getNames()[0].replaceFirst("/", ""), containerName)).toList();
        }
    }

    public Container createContainer(String name, String image, List<String> env, Map<Integer, Integer> ports,
                                     HealthCheck healthCheck, Map<String, String> labels,
                                     List<DockerComposeVolume> volumes, String network, RestartPolicy restartPolicy,
                                     PULL_IMAGE pullImage, String cpus, String cpu_percent, String mem_limit, String mem_reservation,
                                     String dockerCommand) throws InterruptedException {
        List<Container> containers = findContainer(name);
        if (containers.isEmpty()) {
            if (Objects.equals(labels.get(LABEL_TYPE), ContainerType.devmode.name())
                    || Objects.equals(labels.get(LABEL_TYPE), ContainerType.build.name())
                    || Objects.equals(labels.get(LABEL_TYPE), ContainerType.devservice.name())) {
                log.info("Pulling DevMode image from DockerHub: " + image);
                pullImageFromDockerHub(image, Objects.equals(pullImage, PULL_IMAGE.always));
            }
            if (Objects.equals(labels.get(LABEL_TYPE), ContainerType.packaged.name())) {
                log.info("Pulling Project image from Registry: " + image);
                pullImage(image, Objects.equals(pullImage, PULL_IMAGE.always));
            }

            Ports portBindings = DockerUtils.getPortBindings(ports);
            List<ExposedPort> exposePorts = DockerUtils.getExposedPorts(ports);
            try (CreateContainerCmd createContainerCmd = getDockerClient().createContainerCmd(image)
                    .withName(name).withLabels(labels).withEnv(env).withHostName(name).withExposedPorts(exposePorts).withHealthcheck(healthCheck)) {

                List<Mount> mounts = new ArrayList<>();
                if (volumes != null && !volumes.isEmpty()) {
                    volumes.forEach(volume -> {
                        var mount = new Mount().withType(MountType.valueOf(volume.getType().toUpperCase())).withTarget(volume.getTarget());
                        if (volume.getSource() != null) {
                            mount = mount.withSource(volume.getSource());
                        }
                        mounts.add(mount);
                    });
                }
                if (dockerCommand != null) {
                    createContainerCmd.withCmd("/bin/sh", "-c", dockerCommand);
                    log.debug("Docker command: {}", dockerCommand);
                }
                if (Objects.equals(labels.get(LABEL_PROJECT_ID), ContainerType.build.name())) {
                    mounts.add(new Mount().withType(MountType.BIND).withSource("/var/run/docker.sock").withTarget("/var/run/docker.sock"));
                }

                createContainerCmd.withHostConfig(new HostConfig()
                        .withRestartPolicy(restartPolicy)
                        .withPortBindings(portBindings)
                        .withMounts(mounts)
                        .withMemory(DockerUtils.parseMemory(mem_limit))
                        .withMemoryReservation(DockerUtils.parseMemory(mem_reservation))
                        .withCpuPercent(NumberUtils.toLong(cpu_percent))
                        .withNanoCPUs(NumberUtils.toLong(cpus))
                        .withNetworkMode(network != null ? network : properties.docker().network()));

                CreateContainerResponse response = createContainerCmd.exec();
                log.info("Container created: " + response.getId());

                try (ListContainersCmd cmd = getDockerClient().listContainersCmd().withShowAll(true).withIdFilter(Collections.singleton(response.getId()))) {
                    return cmd.exec().get(0);
                }
            }
        } else {
            log.info("Container already exists: " + containers.get(0).getId());
            return containers.get(0);
        }
    }

    public void runContainer(String name) {
        List<Container> containers = findContainer(name);
        if (containers.size() == 1) {
            runContainer(containers.get(0));
        }
    }

    public void runContainer(Container container) {
        if (container.getState().equals("paused")) {
            try (UnpauseContainerCmd cmd = getDockerClient().unpauseContainerCmd(container.getId())) {
                cmd.exec();
            }
        } else if (!container.getState().equals("running")) {
            try (StartContainerCmd cmd = getDockerClient().startContainerCmd(container.getId())) {
                cmd.exec();
            }
        }
    }

    protected void copyFiles(String containerId, String containerPath, Map<String, String> files, boolean dirChildrenOnly) throws IOException {
        String temp = codeService.saveProjectFilesInTemp(files);
        dockerClient.copyArchiveToContainerCmd(containerId).withRemotePath(containerPath)
                .withDirChildrenOnly(dirChildrenOnly).withHostResource(temp).exec();
    }

    public void copyExecFile(String containerId, String containerPath, String filename, String script) {
        String temp = vertx.fileSystem().createTempDirectoryBlocking(containerId);
        String path = temp + File.separator + filename;
        vertx.fileSystem().writeFileBlocking(path, Buffer.buffer(script));

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarArchive = new TarArchiveOutputStream(byteArrayOutputStream)) {
            tarArchive.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            tarArchive.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

            TarArchiveEntry tarEntry = new TarArchiveEntry(new File(path));
            tarEntry.setName(filename);
            tarEntry.setMode(0755);
            tarArchive.putArchiveEntry(tarEntry);
            IOUtils.write(Files.readAllBytes(Paths.get(path)), tarArchive);
            tarArchive.closeArchiveEntry();
            tarArchive.finish();

            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withTarInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
                    .withRemotePath(containerPath).exec();
        } catch (Exception e) {
            log.error("Error copying exec file to container: {}", e.getMessage(), e);
        }
    }

    public void logContainer(String containerName, DockerLogCallback callback) {
        try {
            Container container = getContainerByName(containerName);
            if (container != null) {
                try (LogContainerCmd cmd = getDockerClient().logContainerCmd(container.getId())
                        .withStdOut(true)
                        .withStdErr(true)
                        .withTimestamps(false)
                        .withFollowStream(true)
                        .withTail(100)) {
                    cmd.exec(callback);
                    callback.awaitCompletion();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void pauseContainer(String name) {
        List<Container> containers = findContainer(name);
        if (containers.size() == 1) {
            Container container = containers.get(0);
            if (container.getState().equals("running")) {
                try (PauseContainerCmd cmd = getDockerClient().pauseContainerCmd(container.getId())) {
                    cmd.exec();
                }
            }
        }
    }

    public void stopContainer(String name) {
        List<Container> containers = findContainer(name);
        if (containers.size() == 1) {
            Container container = containers.get(0);
            if (container.getState().equals("running") || container.getState().equals("paused")) {
                try (StopContainerCmd cmd = getDockerClient().stopContainerCmd(container.getId()).withTimeout(1)) {
                    cmd.exec();
                }
            }
        }
    }

    public void deleteContainer(String name) {
        List<Container> containers = findContainer(name);
        if (containers.size() == 1) {
            Container container = containers.get(0);
            try (RemoveContainerCmd cmd = getDockerClient().removeContainerCmd(container.getId()).withForce(true)) {
                cmd.exec();
            }
        }
    }

    public void execCommandInContainer(String containerName, String cmd) throws InterruptedException {
        List<Container> containers = findContainer(containerName);
        if (containers.size() == 1) {
            Container container = containers.get(0);
            if (container.getState().equals("running")) {
                try (ExecCreateCmd execCreateCmd = getDockerClient().execCreateCmd(container.getId()).withAttachStdout(true).withAttachStderr(true).withCmd(cmd.split("\\s+"))) {
                    var execCreateCmdResponse = execCreateCmd.exec();
                    try (ExecStartCmd execStartCmd = getDockerClient().execStartCmd(execCreateCmdResponse.getId())) {
                        execStartCmd.exec(new ExecStartResultCallback(System.out, System.err)).awaitCompletion();
                    }


                }
            }
        }
    }

    public void pullImage(String image, boolean pullAlways) throws InterruptedException {
        try (ListImagesCmd cmd = getDockerClient().listImagesCmd().withShowAll(true)) {
            List<Image> images = cmd.exec();
            List<String> tags = images.stream()
                    .map(i -> Arrays.stream(i.getRepoTags()).collect(Collectors.toList()))
                    .flatMap(Collection::stream)
                    .toList();

            if (pullAlways || images.stream().noneMatch(i -> tags.contains(image))) {
                var callback = new DockerPullCallback(log::info);
                try (PullImageCmd pullImageCmd = getDockerClient().pullImageCmd(image)) {
                    pullImageCmd.exec(callback);
                    callback.awaitCompletion();
                }
            }
        }
    }

    public void pullImageFromDockerHub(String image, boolean pullAlways) throws InterruptedException {
        try (ListImagesCmd cmd = getDockerClientNotConnectedToRegistry().listImagesCmd().withShowAll(true)) {
            List<Image> images = cmd.exec();
            List<String> tags = images.stream()
                    .filter(i -> i.getRepoTags() != null)
                    .map(i -> Arrays.stream(i.getRepoTags()).collect(Collectors.toList()))
                    .flatMap(Collection::stream)
                    .toList();

            if (pullAlways || images.stream().noneMatch(i -> tags.contains(image))) {
                var callback = new DockerPullCallback(log::info);
                try (PullImageCmd pullImageCmd = getDockerClientNotConnectedToRegistry().pullImageCmd(image)) {
                    pullImageCmd.exec(callback);
                    callback.awaitCompletion();
                }
            }
        }
    }

    public void pullImagesForProject(String projectId) throws InterruptedException {
        if (!Objects.equals(properties.containerImage().registry(), "registry:5000") && properties.containerImage().registryUsername().isPresent() && properties.containerImage().registryPassword().isPresent()) {
            var repository = properties.containerImage().registry() + "/" + properties.containerImage().group() + "/" + projectId;
            try (PullImageCmd cmd = getDockerClient().pullImageCmd(repository)) {
                var callback = new DockerPullCallback(log::info);
                cmd.exec(callback);
                callback.awaitCompletion();
            }
        }
    }

    public DockerClient getDockerClientNotConnectedToRegistry() {
        if (dockerClientConnectedToRegistry == null) {
            synchronized (this) {
                if (dockerClientConnectedToRegistry == null) {
                    DockerClientConfig config = getDockerClientConfig(false);
                    DockerHttpClient httpClient = getDockerHttpClient(config);
                    dockerClientConnectedToRegistry = DockerClientImpl.getInstance(config, httpClient);
                }
            }
        }
        return dockerClientConnectedToRegistry;
    }

    public int getMaxPortMapped(int port) {
        try (ListContainersCmd cmd = getDockerClient().listContainersCmd().withShowAll(true)) {
            return cmd.exec().stream()
                    .map(c -> List.of(c.ports))
                    .flatMap(List::stream)
                    .filter(p -> Objects.equals(p.getPrivatePort(), port))
                    .map(ContainerPort::getPublicPort).filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .max().orElse(port);
        }
    }

    public List<ContainerImage> getImages() {
        try (ListImagesCmd cmd = getDockerClient().listImagesCmd().withShowAll(true)) {
            return cmd.exec().stream()
                    .filter(image -> image != null && image.getRepoTags() != null && image.getRepoTags().length > 0)
                    .map(image -> new ContainerImage(image.getId(), image.getRepoTags()[0], image.getCreated(), image.getSize()))
                    .toList();
        }
    }

    public void deleteImage(String imageName) {
        try (ListImagesCmd listImagesCmd = getDockerClient().listImagesCmd().withShowAll(true)) {
            Optional<Image> image = listImagesCmd.exec().stream()
                    .filter(i -> Arrays.asList(i.getRepoTags()).contains(imageName)).findFirst();
            if (image.isPresent()) {
                try (RemoveImageCmd removeImageCmd = getDockerClient().removeImageCmd(image.get().getId())) {
                    removeImageCmd.exec();
                }
            }
        }
    }

    public enum PULL_IMAGE {
        always, ifNotExists, never
    }
}
