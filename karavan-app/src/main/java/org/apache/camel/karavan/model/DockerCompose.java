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
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Docker Compose configuration representing a complete docker-compose.yml file")
public class DockerCompose {

    @Schema(description = "Docker Compose file format version", example = "3.8")
    private String version;

    @Schema(description = "Map of service definitions keyed by service name")
    private Map<String, DockerComposeService> services;

    @Schema(description = "Map of network definitions keyed by network name")
    private Map<String, DockerComposeNetwork> networks;

    public DockerCompose(Map<String, DockerComposeService> services) {
        this.services = services;
    }

    public static DockerCompose create(DockerComposeService service) {
        Map<String, DockerComposeService> map = new HashMap<>();
        map.put(service.getContainer_name(), service);
        return new DockerCompose(map);
    }
}
