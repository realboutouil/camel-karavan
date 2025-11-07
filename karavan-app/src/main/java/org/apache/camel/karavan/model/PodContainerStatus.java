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

package org.apache.camel.karavan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Represents the status and configuration of a pod container")
public class PodContainerStatus {

    @Schema(description = "The unique identifier of the project", example = "my-camel-project")
    private String projectId;
    @Schema(description = "The name of the container", example = "camel-container-1")
    private String containerName;
    @Schema(description = "The unique identifier of the container instance", example = "abc123def456")
    private String containerId;
    @Schema(description = "The container image name and tag", example = "camel-karavan:latest")
    private String image;
    @Schema(description = "List of ports exposed by the container")
    private List<ContainerPort> ports;
    @Schema(description = "The environment where the container is running", example = "dev")
    private String env;
    @Schema(description = "The type of container")
    private ContainerType type;
    @Schema(description = "Memory usage information", example = "512Mi/1Gi")
    private String memoryInfo;
    @Schema(description = "CPU usage information", example = "200m/500m")
    private String cpuInfo;
    @Schema(description = "Timestamp when the container was created", example = "2025-11-07T10:30:00Z")
    private String created;
    @Schema(description = "Timestamp when the container finished", example = "2025-11-07T12:30:00Z")
    private String finished;
    @Schema(description = "List of available commands for this container")
    private List<Command> commands;
    @Schema(description = "Current state of the container", example = "running")
    private String state;
    @Schema(description = "Current phase of the pod", example = "Running")
    private String phase;
    @Schema(description = "Indicates whether the code has been loaded into the container", example = "true")
    private Boolean codeLoaded;
    @Builder.Default
    @Schema(description = "Indicates whether the container is in transit", example = "false")
    private Boolean inTransit = false;
    @Schema(description = "Timestamp when the container was initialized", example = "2025-11-07T10:30:00Z")
    private String initDate;
    @Schema(description = "The IP address assigned to the pod", example = "10.244.0.5")
    private String podIP;
    @Schema(description = "The Camel runtime version", example = "4.14.2")
    private String camelRuntime;
    @Schema(description = "The git commit hash of the deployed code", example = "a1b2c3d4")
    private String commit;
    @Schema(description = "Key-value pairs of labels associated with the container")
    private Map<String, String> labels;

    // Custom constructor for backward compatibility (used in PodEventHandler)
    public PodContainerStatus(String containerName, List<Command> commands, String projectId, String env,
                              ContainerType type, String memoryInfo, String cpuInfo, String created) {
        this.containerName = containerName;
        this.commands = commands;
        this.projectId = projectId;
        this.env = env;
        this.type = type;
        this.memoryInfo = memoryInfo;
        this.cpuInfo = cpuInfo;
        this.created = created;
        this.inTransit = false;
    }

    public static PodContainerStatus createDevMode(String projectId, String env) {
        return PodContainerStatus.builder()
                .projectId(projectId)
                .containerName(projectId)
                .env(env)
                .type(ContainerType.devmode)
                .commands(List.of(Command.run))
                .codeLoaded(false)
                .inTransit(false)
                .initDate("")
                .labels(new HashMap<>())
                .build();
    }

    public static PodContainerStatus createByType(String name, String env, ContainerType type) {
        return PodContainerStatus.builder()
                .projectId(name)
                .containerName(name)
                .env(env)
                .type(type)
                .commands(List.of(Command.run))
                .codeLoaded(false)
                .inTransit(false)
                .initDate("")
                .labels(new HashMap<>())
                .build();
    }

    public static PodContainerStatus createWithId(String projectId, String containerName, String env,
                                                  String containerId, String image, List<ContainerPort> ports,
                                                  ContainerType type, List<Command> commands, String status,
                                                  String created, String camelRuntime, Map<String, String> labels) {
        return PodContainerStatus.builder()
                .projectId(projectId)
                .containerName(containerName)
                .containerId(containerId)
                .image(image)
                .ports(ports)
                .env(env)
                .type(type)
                .created(created)
                .commands(commands)
                .state(status)
                .codeLoaded(false)
                .inTransit(false)
                .camelRuntime(camelRuntime)
                .labels(labels)
                .build();
    }

    public PodContainerStatus copy() {
        return PodContainerStatus.builder()
                .projectId(projectId)
                .containerName(containerName)
                .containerId(containerId)
                .image(image)
                .ports(ports)
                .env(env)
                .type(type)
                .memoryInfo(memoryInfo)
                .cpuInfo(cpuInfo)
                .created(created)
                .finished(finished)
                .commands(commands)
                .state(state)
                .phase(phase)
                .codeLoaded(codeLoaded)
                .inTransit(inTransit)
                .initDate(initDate)
                .podIP(podIP)
                .camelRuntime(camelRuntime)
                .commit(commit)
                .labels(labels)
                .build();
    }

    @Schema(description = "Possible states of a container")
    public enum State {
        created,
        running,
        restarting,
        paused,
        exited,
        dead
    }

    @Schema(description = "Available commands that can be executed on a container")
    public enum Command {
        run,
        pause,
        stop,
        delete,
    }
}
