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

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Karavan integration project")
public class Project {

    @Schema(description = "Unique project identifier", required = true, example = "my-integration-project")
    private String projectId;
    @Schema(description = "Human-readable project name", required = true, example = "My Integration Project")
    private String name;
    @Schema(description = "Last Git commit hash", example = "a1b2c3d4e5f6")
    private String lastCommit;
    @Schema(description = "Timestamp of last commit in milliseconds since epoch", example = "1699876543210")
    private Long lastCommitTimestamp;
    @Schema(description = "Project type", required = true, defaultValue = "normal")
    @Builder.Default
    private Type type = Type.normal;

    public Project(String projectId, String name, String lastCommit, Long lastCommitTimestamp) {
        this.projectId = projectId;
        this.name = name;
        this.lastCommit = lastCommit;
        this.lastCommitTimestamp = lastCommitTimestamp;
        this.type = Arrays.stream(Type.values()).anyMatch(t -> t.name().equals(projectId))
                ? Type.valueOf(projectId) : Type.normal;
    }

    public Project(String projectId, String name) {
        this.projectId = projectId;
        this.name = name;
        this.lastCommitTimestamp = Instant.now().toEpochMilli();
        this.type = Arrays.stream(Type.values()).anyMatch(t -> t.name().equals(projectId))
                ? Type.valueOf(projectId) : Type.normal;
    }

    @Schema(description = "Get list of built-in project names")
    public static List<String> getBuildInNames() {
        return List.of(
                Type.configuration.name(),
                Type.kamelets.name(),
                Type.templates.name()
        );
    }

    @Schema(description = "Get package suffix for GAV coordinates based on project ID")
    public String getGavPackageSuffix() {
        return projectId.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    @Schema(description = "Create a copy of the project")
    public Project copy() {
        return new Project(projectId, name, lastCommit, lastCommitTimestamp, type);
    }

    @Schema(description = "Project types in Karavan")
    public enum Type {
        templates,
        kamelets,
        configuration,
        services,
        normal,
    }
}
