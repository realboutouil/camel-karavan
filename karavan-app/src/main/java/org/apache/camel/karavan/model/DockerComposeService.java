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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Docker Compose service definition representing a container service configuration")
public class DockerComposeService {

    @Schema(description = "Name of the container", example = "my-service-container")
    private String container_name;

    @Schema(description = "Docker image to use for the container", example = "nginx:latest")
    private String image;

    @Schema(description = "Restart policy for the container", example = "unless-stopped")
    private String restart;

    @Schema(description = "Number of CPUs to allocate", example = "2")
    private String cpus;

    @Schema(description = "CPU usage percentage limit", example = "50")
    private String cpu_percent;

    @Schema(description = "Memory limit for the container", example = "512m")
    private String mem_limit;

    @Schema(description = "Memory reservation for the container", example = "256m")
    private String mem_reservation;

    @Schema(description = "Command to override the default container command", example = "/bin/sh -c 'echo hello'")
    private String command;

    @Builder.Default
    @Schema(description = "List of port mappings in format host:container", example = "[\"8080:80\", \"8443:443\"]")
    private List<String> ports = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of volume mounts for the container")
    private List<DockerComposeVolume> volumes = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of ports to expose without publishing to host", example = "[\"80\", \"443\"]")
    private List<String> expose = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of service names this service depends on", example = "[\"database\", \"cache\"]")
    private List<String> depends_on = new ArrayList<>();

    @Builder.Default
    @Schema(description = "List of networks this service is connected to", example = "[\"frontend\", \"backend\"]")
    private List<String> networks = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Environment variables as key-value pairs", example = "{\"NODE_ENV\": \"production\", \"PORT\": \"3000\"}")
    private Map<String, String> environment = new HashMap<>();

    @Schema(description = "Health check configuration for the container")
    private DockerComposeHealthCheck healthcheck;

    @Builder.Default
    @Schema(description = "Labels to apply to the container", example = "{\"com.example.version\": \"1.0\", \"com.example.team\": \"backend\"}")
    private Map<String, String> labels = new HashMap<>();

    public Map<Integer, Integer> getPortsMap() {
        Map<Integer, Integer> p = new HashMap<>();
        if (ports != null && !ports.isEmpty()) {
            ports.forEach(s -> {
                String[] values = s.split(":");
                p.put(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
            });
        }
        return p;
    }

    public List<String> getEnvironmentList() {
        return environment != null
                ? environment.entrySet().stream().map(e -> e.getKey().concat("=").concat(e.getValue())).collect(Collectors.toList())
                : new ArrayList<>();
    }

    public void addEnvironment(String key, String value) {
        Map<String, String> map = getEnvironment();
        map.put(key, value);
        setEnvironment(map);
    }

    public Map<String, String> getEnvironment() {
        return environment != null ? environment : new HashMap<>();
    }
}
