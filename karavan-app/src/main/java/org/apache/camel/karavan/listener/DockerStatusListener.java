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

package org.apache.camel.karavan.listener;

import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.core.InvocationBuilder;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.karavan.KaravanCache;
import org.apache.camel.karavan.docker.DockerService;
import org.apache.camel.karavan.docker.DockerUtils;
import org.apache.camel.karavan.config.KaravanProperties;
import org.apache.camel.karavan.model.PodContainerStatus;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.apache.camel.karavan.KaravanEvents.*;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DockerStatusListener {

    private final KaravanProperties properties;
    private final DockerService dockerService;
    private final KaravanCache karavanCache;
    private final EventBus eventBus;

    @ConsumeEvent(value = CMD_COLLECT_CONTAINER_STATISTIC, blocking = true)
    void collectContainersStatistics(JsonObject data) {
        try {
            if (data == null) {
                log.warn("Received null data for container statistics collection");
                return;
            }
            PodContainerStatus status = data.mapTo(PodContainerStatus.class);
            if (status == null) {
                log.warn("Failed to map data to PodContainerStatus");
                return;
            }
            PodContainerStatus newStatus = getContainerStatistics(status);
            if (newStatus != null) {
                eventBus.publish(POD_CONTAINER_UPDATED, JsonObject.mapFrom(newStatus));
            }
        } catch (Exception e) {
            log.error("Error collecting container statistics", e);
        }
    }

    public PodContainerStatus getContainerStatistics(PodContainerStatus podContainerStatus) {
        try {
            if (podContainerStatus == null || podContainerStatus.getContainerName() == null) {
                log.warn("Invalid pod container status or container name is null");
                return podContainerStatus;
            }
            Container container = dockerService.getContainerByName(podContainerStatus.getContainerName());
            if (container == null) {
                log.warn("Container not found: {}", podContainerStatus.getContainerName());
                return podContainerStatus;
            }
            Statistics stats = getContainerStats(container.getId());
            if (stats != null) {
                DockerUtils.updateStatistics(podContainerStatus, stats);
            }
            return podContainerStatus;
        } catch (Exception e) {
            log.error("Error getting container statistics for: {}",
                    podContainerStatus != null ? podContainerStatus.getContainerName() : "unknown", e);
            return podContainerStatus;
        }
    }

    public Statistics getContainerStats(String containerId) {
        if (containerId == null || containerId.isEmpty()) {
            log.warn("Container ID is null or empty, cannot fetch stats");
            return null;
        }

        InvocationBuilder.AsyncResultCallback<Statistics> callback = new InvocationBuilder.AsyncResultCallback<>();
        Statistics stats = null;
        try {
            dockerService.getDockerClient().statsCmd(containerId).withContainerId(containerId).withNoStream(true).exec(callback);
            stats = callback.awaitResult();
            callback.close();
        } catch (RuntimeException e) {
            log.error("Runtime error getting stats for container: {}", containerId, e);
        } catch (IOException e) {
            log.error("IO error getting stats for container: {}", containerId, e);
        } catch (Exception e) {
            log.error("Unexpected error getting stats for container: {}", containerId, e);
        }
        return stats;
    }

    @ConsumeEvent(value = CMD_CLEAN_STATUSES, blocking = true)
    void cleanContainersStatuses(String data) {
        try {
            List<PodContainerStatus> statusesInDocker = getContainersStatuses();
            if (statusesInDocker == null) {
                log.warn("Container statuses in Docker is null, skipping cleanup");
                return;
            }

            List<String> namesInDocker = statusesInDocker.stream()
                    .filter(status -> status != null && status.getContainerName() != null)
                    .map(PodContainerStatus::getContainerName)
                    .toList();

            List<PodContainerStatus> statusesInCache = karavanCache.getPodContainerStatuses(properties.environment());
            if (statusesInCache == null) {
                log.debug("No container statuses found in cache, skipping cleanup");
                return;
            }

            // clean deleted
            statusesInCache.stream()
                    .filter(cs -> cs != null)
                    .filter(cs -> !checkTransit(cs))
                    .filter(cs -> cs.getContainerName() != null && !namesInDocker.contains(cs.getContainerName()))
                    .forEach(containerStatus -> {
                        try {
                            eventBus.publish(POD_CONTAINER_DELETED, JsonObject.mapFrom(containerStatus));
                        } catch (Exception e) {
                            log.error("Error publishing container deleted event for container: {}",
                                    containerStatus.getContainerName(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("Error cleaning container statuses", e);
        }
    }

    public List<PodContainerStatus> getContainersStatuses() {
        List<PodContainerStatus> result = new ArrayList<>();
        try {
            List<Container> containers = dockerService.getAllContainers();
            if (containers == null) {
                log.warn("Docker service returned null container list");
                return result;
            }
            containers.forEach(container -> {
                try {
                    if (container != null) {
                        PodContainerStatus podContainerStatus = DockerUtils.getContainerStatus(container, properties.environment());
                        if (podContainerStatus != null) {
                            result.add(podContainerStatus);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error getting status for container: {}",
                            container.getId(), e);
                }
            });
        } catch (Exception e) {
            log.error("Error getting all container statuses", e);
        }
        return result;
    }

    private boolean checkTransit(PodContainerStatus cs) {
        try {
            if (cs != null && cs.getContainerId() == null && cs.getInTransit() != null && cs.getInTransit()) {
                if (cs.getInitDate() != null && !cs.getInitDate().isBlank()) {
                    return Instant.parse(cs.getInitDate()).until(Instant.now(), ChronoUnit.SECONDS) < 10;
                }
            }
        } catch (Exception e) {
            log.warn("Error checking transit status for container: {}",
                    cs.getContainerName(), e);
        }
        return false;
    }
}