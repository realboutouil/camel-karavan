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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Deployment status of a project")
public class DeploymentStatus {

    @Schema(description = "Project identifier", required = true, example = "my-integration-project")
    private String projectId;

    @Schema(description = "Kubernetes namespace or Docker network", required = true, example = "default")
    private String namespace;

    @Schema(description = "Environment name", required = true, example = "dev")
    private String env;

    @Schema(description = "Cluster name", required = true, example = "local")
    private String cluster;

    @Schema(description = "Container image name and tag", example = "registry:5000/karavan/my-project:latest")
    @Builder.Default
    private String image = "";

    @Schema(description = "Desired number of replicas", example = "1")
    @Builder.Default
    private Integer replicas = 0;

    @Schema(description = "Number of ready replicas", example = "1")
    @Builder.Default
    private Integer readyReplicas = 0;

    @Schema(description = "Number of unavailable replicas", example = "0")
    @Builder.Default
    private Integer unavailableReplicas = 0;

    @Schema(description = "Container type (Docker or Kubernetes)", required = true)
    private ContainerType type;

    public DeploymentStatus(String projectId, String namespace, String cluster, String env) {
        this.projectId = projectId;
        this.namespace = namespace;
        this.cluster = cluster;
        this.env = env;
        this.image = "";
        this.replicas = 0;
        this.readyReplicas = 0;
        this.unavailableReplicas = 0;
    }

    @Schema(description = "Create a copy of the deployment status")
    public DeploymentStatus copy() {
        return new DeploymentStatus(projectId, namespace, cluster, env, image, replicas, readyReplicas, unavailableReplicas, type);
    }
}
