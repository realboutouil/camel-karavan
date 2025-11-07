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

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Docker Compose health check configuration for container health monitoring")
public class DockerComposeHealthCheck {

    @Schema(description = "Time between health checks", example = "30s")
    private String interval;

    @Schema(description = "Number of consecutive failures needed to mark container as unhealthy", example = "3")
    private Integer retries;

    @Schema(description = "Maximum time to wait for a health check", example = "10s")
    private String timeout;

    @Schema(description = "Grace period before starting health checks", example = "40s")
    private String start_period;

    @Schema(description = "Command to run to check health (e.g., CMD-SHELL, CMD)", example = "[\"CMD-SHELL\", \"curl -f http://localhost:8080/health || exit 1\"]")
    private List<String> test;
}
