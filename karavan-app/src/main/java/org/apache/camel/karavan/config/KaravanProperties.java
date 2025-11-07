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
package org.apache.camel.karavan.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.util.List;
import java.util.Optional;

import static org.apache.camel.karavan.KaravanConstants.DEV;

@ConfigMapping(prefix = "karavan")
public interface KaravanProperties {

    /**
     * Application name
     */
    @WithName("appName")
    @WithDefault("karavan")
    String appName();

    /**
     * Application title
     */
    @WithDefault("Apache Camel Karavan")
    String title();

    /**
     * Application version
     */
    @WithDefault("4.14.2")
    String version();

    /**
     * Authentication type (public or sessionId or oidc)
     */
    @WithDefault("public")
    String auth();

    /**
     * Current environment (dev, prod, kubernetes, openshift)
     */
    @WithDefault(DEV)
    String environment();

    /**
     * List of available environments
     */
    Optional<List<String>> environments();

    /**
     * Shared folder path for configuration files
     */
    @WithName("shared.folder")
    Optional<String> sharedFolder();

    /**
     * Group Artifact Version for Maven projects
     */
    Optional<String> gav();

    /**
     * Path to private key file for SSH authentication
     */
    @WithName("private-key-path")
    Optional<String> privateKeyPath();

    /**
     * Path to known hosts file for SSH authentication
     */
    @WithName("known-hosts-path")
    Optional<String> knownHostsPath();

    /**
     * Whether running in OpenShift
     */
    Optional<Boolean> openshift();

    /**
     * Infrastructure type (docker or kubernetes)
     */
    Optional<String> infrastructure();

    /**
     * Whether to install Gitea for local development
     */
    @WithName("git-install-gitea")
    @WithDefault("false")
    boolean gitInstallGitea();

    /**
     * Whether to install image registry for local development
     */
    @WithName("image-registry-install")
    @WithDefault("false")
    boolean imageRegistryInstall();

    /**
     * Kubernetes secret name
     */
    @WithName("secret.name")
    @WithDefault("karavan")
    String secretName();

    /**
     * Git repository configuration
     */
    Git git();

    /**
     * Container image registry configuration
     */
    @WithName("container-image")
    ContainerImage containerImage();

    /**
     * DevMode configuration
     */
    DevMode devmode();

    /**
     * Builder configuration
     */
    Builder builder();

    /**
     * Docker configuration
     */
    Docker docker();

    /**
     * Cache configuration
     */
    Cache cache();

    /**
     * Camel status check interval configuration
     */
    @WithName("camel.status.interval")
    @WithDefault("2s")
    String camelStatusInterval();

    /**
     * Container status check interval configuration
     */
    @WithName("container.status.interval")
    @WithDefault("2s")
    String containerStatusInterval();

    /**
     * Container statistics check interval configuration
     */
    @WithName("container.statistics.interval")
    @WithDefault("10s")
    String containerStatisticsInterval();

    /**
     * Keycloak configuration
     */
    Keycloak keycloak();

    /**
     * Git repository configuration
     */
    interface Git {
        /**
         * Git repository URL
         */
        Optional<String> repository();

        /**
         * Git username for authentication
         */
        Optional<String> username();

        /**
         * Git password for authentication
         */
        Optional<String> password();

        /**
         * Git branch to use
         */
        @WithDefault("main")
        String branch();

        /**
         * Whether to use ephemeral (in-memory) git storage
         */
        @WithDefault("false")
        boolean ephemeral();
    }

    /**
     * Container image registry configuration
     */
    interface ContainerImage {
        /**
         * Container registry URL
         */
        String registry();

        /**
         * Container image group/namespace
         */
        String group();

        /**
         * Registry username for authentication
         */
        @WithName("registry-username")
        Optional<String> registryUsername();

        /**
         * Registry password for authentication
         */
        @WithName("registry-password")
        Optional<String> registryPassword();
    }

    /**
     * DevMode configuration
     */
    interface DevMode {
        /**
         * DevMode container image
         */
        @WithDefault("ghcr.io/apache/camel-karavan-devmode:4.14.2")
        String image();

        /**
         * DevMode image pull policy
         */
        @WithName("image-pull-policy")
        @WithDefault("IfNotPresent")
        String imagePullPolicy();

        /**
         * DevMode service account
         */
        @WithName("service.account")
        @WithDefault("karavan")
        String serviceAccount();

        /**
         * Whether to create Maven m2 PVC for devmode
         */
        @WithName("createm2")
        @WithDefault("false")
        boolean createM2();
    }

    /**
     * Builder configuration
     */
    interface Builder {
        /**
         * Builder service account
         */
        @WithName("service.account")
        @WithDefault("karavan")
        String serviceAccount();
    }

    /**
     * Docker configuration
     */
    interface Docker {
        /**
         * Docker network name
         */
        @WithDefault("karavan")
        String network();
    }

    /**
     * Cache configuration
     */
    interface Cache {
        /**
         * Cache state persistence path
         */
        @WithName("state.path")
        @WithDefault("./cache-data")
        String statePath();
    }

    /**
     * Keycloak configuration
     */
    interface Keycloak {
        /**
         * Keycloak server URL
         */
        @WithDefault("http://localhost:8079")
        String url();

        /**
         * Keycloak realm name
         */
        @WithDefault("karavan")
        String realm();

        /**
         * Frontend configuration
         */
        Frontend frontend();

        /**
         * Backend configuration
         */
        Backend backend();

        interface Frontend {
            /**
             * Frontend client ID
             */
            @WithName("clientId")
            @WithDefault("frontend")
            String clientId();
        }

        interface Backend {
            /**
             * Backend client ID
             */
            @WithName("clientId")
            @WithDefault("backend")
            String clientId();

            /**
             * Backend client secret
             */
            @WithDefault("karavan")
            String secret();
        }
    }
}
