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
@Schema(description = "Composite key for grouping resources by project, environment, and key identifier")
public class GroupedKey {

    @Schema(description = "Unique identifier of the project", example = "camel-karavan-app")
    private String projectId;

    @Schema(description = "Environment name (e.g., dev, staging, production)", example = "dev")
    private String env;

    @Schema(description = "Unique key identifier within the project and environment", example = "deployment-config")
    private String key;

    /**
     * Factory method to create a cache key string from components
     *
     * @param projectId the project identifier
     * @param env       the environment name
     * @param key       the key identifier
     * @return formatted cache key string
     */
    public static String create(String projectId, String env, String key) {
        return new GroupedKey(projectId, env, key).getCacheKey();
    }

    /**
     * Generates a cache key by concatenating projectId, env, and key with colons
     *
     * @return formatted cache key string
     */
    public String getCacheKey() {
        return projectId + ":" + env + ":" + key;
    }

    @Override
    public int hashCode() {
        return getCacheKey().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupedKey that = (GroupedKey) o;

        if (!projectId.equals(that.projectId)) return false;
        if (!env.equals(that.env)) return false;
        return key.equals(that.key);
    }

    @Override
    public String toString() {
        return "GroupedKey{" + getCacheKey() + '}';
    }
}