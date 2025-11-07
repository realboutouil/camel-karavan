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
@Schema(description = "Container image information from Docker registry or local cache")
public class ContainerImage {

    @Schema(description = "Unique identifier of the container image", example = "sha256:abc123def456...")
    private String id;

    @Schema(description = "Image tag/version", example = "nginx:1.21.0")
    private String tag;

    @Schema(description = "Timestamp when the image was created (Unix epoch in milliseconds)", example = "1638360000000")
    private Long created;

    @Schema(description = "Size of the image in bytes", example = "142000000")
    private Long size;
}