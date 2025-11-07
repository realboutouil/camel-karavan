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
@Schema(description = "Represents the changes made to a file in a Git commit")
public class ProjectFileCommitDiff {

    @Schema(description = "Type of change (ADD, MODIFY, DELETE, RENAME, COPY)", example = "MODIFY")
    private String changeType;

    @Schema(description = "New file path after the change", example = "src/main/routes/order-route.yaml")
    private String newPath;

    @Schema(description = "Original file path before the change", example = "src/main/routes/order-route-old.yaml")
    private String oldPath;

    @Schema(description = "Unified diff format showing the changes", example = "@@ -1,3 +1,4 @@\n- route:\n    from:\n      uri: timer:tick\n+     parameters:\n+       period: 5000")
    private String diff;
}
