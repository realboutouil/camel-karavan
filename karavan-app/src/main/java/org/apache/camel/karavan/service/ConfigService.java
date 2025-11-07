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
package org.apache.camel.karavan.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.karavan.KaravanCache;
import org.apache.camel.karavan.kubernetes.KubernetesService;
import org.apache.camel.karavan.model.Configuration;
import org.apache.camel.karavan.config.KaravanProperties;
import org.apache.camel.karavan.model.Project;
import org.apache.camel.karavan.model.ProjectFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.camel.karavan.KaravanConstants.DEV;
import static org.apache.camel.karavan.service.CodeService.BUILD_SCRIPT_FILENAME;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ConfigService {

    private static Boolean inKubernetes;
    private static Boolean inDocker;
    private final KaravanProperties properties;
    private final KaravanCache karavanCache;
    private final KubernetesService kubernetesService;
    private final CodeService codeService;
    private Configuration configuration;

    public static boolean inDocker() {
        if (inDocker == null) {
            inDocker = !inKubernetes() && Files.exists(Paths.get("/.dockerenv"));
        }
        return inDocker;
    }

    public String getAppName() {
        return properties.appName();
    }

    void onStart(@Observes @Priority(10) StartupEvent ev) {
        getConfiguration(null);
    }

    public Configuration getConfiguration(Map<String, String> advanced) {
        if (configuration == null) {
            var configFilenames = codeService.getConfigurationList();
            configuration = new Configuration(
                    properties.title(),
                    properties.version(),
                    inKubernetes() ? "kubernetes" : "docker",
                    properties.environment(),
                    getEnvs(),
                    configFilenames,
                    advanced
            );
        }
        return configuration;
    }

    public static boolean inKubernetes() {
        if (inKubernetes == null) {
            inKubernetes = Objects.nonNull(System.getenv("KUBERNETES_SERVICE_HOST"));
        }
        return inKubernetes;
    }

    protected List<String> getEnvs() {
        return properties.environments().orElse(List.of(DEV));
    }

    public void shareOnStartup() {
        if (ConfigService.inKubernetes() && properties.environment().equals(DEV)) {
            log.info("Creating Configmap for " + BUILD_SCRIPT_FILENAME);
            try {
                share(BUILD_SCRIPT_FILENAME);
            } catch (Exception e) {
                var error = e.getCause() != null ? e.getCause() : e;
                log.error("Error while trying to share build.sh as Configmap", error);
            }
        }
    }

    public void share(String filename) throws Exception {
        if (filename != null) {
            ProjectFile f = karavanCache.getProjectFile(Project.Type.configuration.name(), filename);
            if (f != null) {
                shareFile(f);
            }
        } else {
            for (ProjectFile f : karavanCache.getProjectFiles(Project.Type.configuration.name())) {
                shareFile(f);
            }
        }
    }

    private void shareFile(ProjectFile f) throws Exception {
        var filename = f.getName();
        var parts = filename.split("\\.");
        var prefix = parts[0];
        if (properties.environment().equals(DEV) && !getEnvs().contains(prefix)) { // no prefix AND dev env
            storeFile(f.getName(), f.getCode());
        } else if (Objects.equals(prefix, properties.environment())) { // with prefix == env
            filename = f.getName().substring(properties.environment().length() + 1);
            storeFile(filename, f.getCode());
        }
    }

    private void storeFile(String filename, String code) throws Exception {
        if (inKubernetes()) {
            createConfigMapFromFile(filename, code);
        } else {
            if (properties.sharedFolder().isPresent()) {
                Files.writeString(Paths.get(properties.sharedFolder().get(), filename), code);
            } else {
                throw new Exception("Shared folder not configured");
            }
        }
    }

    private void createConfigMapFromFile(String filename, String content) {
        kubernetesService.createConfigmap(filename, Map.of(filename, content));
    }
}