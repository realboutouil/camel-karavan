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
@Schema(description = "Git repository representation containing metadata and file list")
public class GitRepo {

    @Schema(description = "Repository name", example = "my-camel-project")
    private String name;

    @Schema(description = "Current commit ID (SHA-1 hash)", example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0")
    private String commitId;

    @Schema(description = "Timestamp of the last commit in milliseconds", example = "1699564800000")
    private Long lastCommitTimestamp;

    @Schema(description = "List of files in the repository")
    private List<GitRepoFile> files;
}
