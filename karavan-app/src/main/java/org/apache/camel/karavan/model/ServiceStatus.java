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
@Schema(description = "Represents the status and configuration of a Kubernetes service")
public class ServiceStatus {

    @Schema(description = "The unique identifier of the project", example = "my-camel-project")
    private String projectId;

    @Schema(description = "The Kubernetes namespace where the service is deployed", example = "default")
    private String namespace;

    @Schema(description = "The environment where the service is running", example = "dev")
    private String env;

    @Schema(description = "The cluster name where the service is deployed", example = "production-cluster")
    private String cluster;

    @Schema(description = "The port exposed by the service", example = "8080")
    private Integer port;

    @Schema(description = "The target port on the pod that the service forwards traffic to", example = "8080")
    private Integer targetPort;

    @Schema(description = "The internal cluster IP address assigned to the service", example = "10.96.0.1")
    private String clusterIP;

    @Schema(description = "The type of Kubernetes service", example = "ClusterIP")
    private String type;

    public ServiceStatus(String projectId, String namespace, String cluster, String env) {
        this.projectId = projectId;
        this.namespace = namespace;
        this.env = env;
        this.cluster = cluster;
    }
}
