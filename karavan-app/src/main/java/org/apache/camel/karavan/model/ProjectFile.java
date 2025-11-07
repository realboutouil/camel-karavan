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
@Schema(description = "File within a Karavan project")
public class ProjectFile {

    @Schema(description = "File name with extension", required = true, example = "routes.camel.yaml")
    private String name;

    @Schema(description = "File content as string", required = true)
    private String code;

    @Schema(description = "Parent project identifier", required = true, example = "my-integration-project")
    private String projectId;

    @Schema(description = "Timestamp of last file update in milliseconds since epoch", example = "1699876543210")
    private Long lastUpdate;

    @Schema(description = "Create a copy of the project file")
    public ProjectFile copy() {
        return new ProjectFile(name, code, projectId, lastUpdate);
    }
}
