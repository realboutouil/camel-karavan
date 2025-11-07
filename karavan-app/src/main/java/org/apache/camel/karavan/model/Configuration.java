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
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Karavan application configuration")
public class Configuration {

    @Schema(description = "Application title", required = true, example = "Apache Camel Karavan")
    private String title;

    @Schema(description = "Application version", required = true, example = "4.14.2")
    private String version;

    @Schema(description = "Infrastructure type (kubernetes or docker)", required = true, example = "docker")
    private String infrastructure;

    @Schema(description = "Current environment name", required = true, example = "dev")
    private String environment;

    @Schema(description = "List of available environments", example = "[\"dev\", \"test\", \"prod\"]")
    private List<String> environments;

    @Schema(description = "List of configuration file names", example = "[\"application.properties\", \"routes.camel.yaml\"]")
    private List<String> configFilenames;

    @Schema(description = "Application status information")
    private List<Object> status;

    @Schema(description = "Advanced configuration properties")
    private Map<String, String> advanced;

    // Custom constructor for backward compatibility (without status field)
    public Configuration(String title, String version, String infrastructure, String environment,
                         List<String> environments, List<String> configFilenames, Map<String, String> advanced) {
        this.title = title;
        this.version = version;
        this.infrastructure = infrastructure;
        this.environment = environment;
        this.environments = environments;
        this.configFilenames = configFilenames;
        this.advanced = advanced;
    }
}
