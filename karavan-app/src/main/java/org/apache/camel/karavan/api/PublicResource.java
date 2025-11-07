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
package org.apache.camel.karavan.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.apache.camel.karavan.KaravanStartupLoader;
import org.apache.camel.karavan.kubernetes.KubernetesStatusService;
import org.apache.camel.karavan.config.KaravanProperties;
import org.eclipse.microprofile.health.HealthCheckResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Path("/public")
public class PublicResource {

    private final KaravanStartupLoader karavanStartupLoader;
    private final KubernetesStatusService kubernetesStatusService;
    private final KaravanProperties properties;

    @GET
    @Path("/auth")
    @Produces(MediaType.TEXT_PLAIN)
    public Response authType() throws Exception {
        return Response.ok(properties.auth()).build();
    }

    @GET
    @Path("/sso-config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ssoConfig() throws Exception {
        Map<String, String> getSsoConfig = Map.of(
                "url", properties.keycloak().url(),
                "realm", properties.keycloak().realm(),
                "clientId", properties.keycloak().frontend().clientId()
        );
        return Response.ok(getSsoConfig).build();
    }

    @GET
    @Path("/readiness")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfiguration() throws Exception {
        List<HealthCheckResponse> list = List.of(
                kubernetesStatusService.call(),
                karavanStartupLoader.call()
        );
        return Response.ok(Map.of(
                "status", list.stream().allMatch(h -> Objects.equals(h.getStatus(), HealthCheckResponse.Status.UP)),
                "checks", list
        )).build();
    }
}