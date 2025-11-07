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
@Schema(description = "Represents the status information of a Camel context and its components")
public class CamelStatus {

    @Schema(description = "The unique identifier of the project", example = "my-camel-project")
    private String projectId;

    @Schema(description = "The name of the container running the Camel context", example = "camel-container-1")
    private String containerName;

    @Schema(description = "List of status values for various Camel components")
    private List<CamelStatusValue> statuses;

    @Schema(description = "The environment where the Camel context is running", example = "dev")
    private String env;

    public CamelStatus copy() {
        return new CamelStatus(this.projectId, this.containerName, List.copyOf(this.statuses), this.env);
    }
}
