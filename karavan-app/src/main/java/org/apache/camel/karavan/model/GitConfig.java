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
@Schema(description = "Git repository configuration containing connection details and authentication information")
public class GitConfig {

    @Schema(description = "Git repository URI", example = "https://github.com/username/repository.git")
    private String uri;

    @Schema(description = "Username for Git authentication", example = "gituser")
    private String username;

    @Schema(description = "Password or personal access token for Git authentication", example = "ghp_xxxxxxxxxxxx")
    private String password;

    @Schema(description = "Git branch to use", example = "main")
    private String branch;

    @Schema(description = "Path to the private SSH key file for Git authentication", example = "/home/user/.ssh/id_rsa")
    private String privateKeyPath;
}
