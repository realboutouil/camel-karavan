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
package org.apache.camel.karavan;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.karavan.docker.DockerService;
import org.apache.camel.karavan.model.GitRepo;
import org.apache.camel.karavan.config.KaravanProperties;
import org.apache.camel.karavan.model.Project;
import org.apache.camel.karavan.model.ProjectFile;
import org.apache.camel.karavan.service.CodeService;
import org.apache.camel.karavan.service.ConfigService;
import org.apache.camel.karavan.service.GitService;
import org.apache.camel.karavan.service.ProjectService;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.camel.karavan.KaravanConstants.DEV;
import static org.apache.camel.karavan.KaravanEvents.NOTIFICATION_PROJECTS_STARTED;

@Slf4j
@Default
@Readiness
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class KaravanStartupLoader implements HealthCheck {

    private final KaravanProperties properties;
    private final ProjectService projectService;
    private final KaravanCache karavanCache;
    private final DockerService dockerService;
    private final GitService gitService;
    private final CodeService codeService;
    private final EventBus eventBus;

    private final AtomicBoolean ready = new AtomicBoolean(false);

    @Override
    public HealthCheckResponse call() {
        if (ready.get()) {
            return HealthCheckResponse.named("Projects").up().build();
        } else {
            return HealthCheckResponse.named("Projects").down().build();
        }
    }

    void onStart(@Observes StartupEvent ev) throws Exception {
        log.info("Starting " + properties.appName() + " in " + properties.environment() + " env in " + (ConfigService.inKubernetes() ? "Kubernetes" : "Docker"));
        if (!ConfigService.inKubernetes() && !dockerService.checkDocker()) {
            Quarkus.asyncExit();
        } else {
            log.info("Projects loading...");
            tryStart();
            eventBus.publish(NOTIFICATION_PROJECTS_STARTED, null);
            log.info("Projects loaded");
        }
    }

    public void tryStart() throws Exception {
        boolean git = gitService.checkGit();
        log.info("Starting Project service: git is " + (git ? "ready" : "not ready"));
        if (gitService.checkGit()) {
            if (karavanCache.getProjects().isEmpty()) {
                importAllProjects();
            }
            if (Objects.equals(properties.environment(), DEV)) {
                addKameletsProject();
                addTemplatesProject();
                addConfigurationProject();
                addServicesProject();
            }
            ready.set(true);
        } else {
            log.info("Projects are not ready");
            throw new Exception("Projects are not ready");
        }
    }

    private void importAllProjects() {
        log.info("Import projects from git: " + gitService.getGitConfig().getUri());
        try {
            List<GitRepo> repos = gitService.readProjectsToImport();
            repos.forEach(repo -> {
                Project project;
                String folderName = repo.getName();
                if (folderName.equals(Project.Type.templates.name())) {
                    project = new Project(Project.Type.templates.name(), "Templates", repo.getCommitId(), repo.getLastCommitTimestamp(), Project.Type.templates);
                } else if (folderName.equals(Project.Type.kamelets.name())) {
                    project = new Project(Project.Type.kamelets.name(), "Custom Kamelets", repo.getCommitId(), repo.getLastCommitTimestamp(), Project.Type.kamelets);
                } else if (folderName.equals(Project.Type.configuration.name())) {
                    project = new Project(Project.Type.configuration.name(), "Configuration", repo.getCommitId(), repo.getLastCommitTimestamp(), Project.Type.configuration);
                } else if (folderName.equals(Project.Type.services.name())) {
                    project = new Project(Project.Type.services.name(), "Dev Services", repo.getCommitId(), repo.getLastCommitTimestamp(), Project.Type.services);
                } else {
                    project = projectService.getProjectFromRepo(repo);
                }
                karavanCache.saveProject(project, true);

                repo.getFiles().forEach(repoFile -> {
                    ProjectFile file = new ProjectFile(repoFile.getName(), repoFile.getBody(), folderName, repoFile.getLastCommitTimestamp());
                    karavanCache.saveProjectFile(file, true, true);
                });
            });
        } catch (Exception e) {
            log.error("Error during project import", e);
        }
    }

    void addKameletsProject() {
        try {
            Project kamelets = karavanCache.getProject(Project.Type.kamelets.name());
            if (kamelets == null) {
                log.info("Add custom kamelets project");
                kamelets = new Project(Project.Type.kamelets.name(), "Custom Kamelets", "", Instant.now().toEpochMilli(), Project.Type.kamelets);
                karavanCache.saveProject(kamelets, true);
            }
        } catch (Exception e) {
            log.error("Error during custom kamelets project creation", e);
        }
    }

    void addTemplatesProject() {
        try {
            Project templates = karavanCache.getProject(Project.Type.templates.name());
            if (templates == null) {
                log.info("Add templates project");
                templates = new Project(Project.Type.templates.name(), "Templates", "", Instant.now().toEpochMilli(), Project.Type.templates);
                karavanCache.saveProject(templates, true);

                codeService.getTemplates().forEach((name, value) -> {
                    ProjectFile file = new ProjectFile(name, value, Project.Type.templates.name(), Instant.now().toEpochMilli());
                    karavanCache.saveProjectFile(file, false, true);
                });
            } else {
                codeService.getTemplates().forEach((name, value) -> {
                    ProjectFile f = karavanCache.getProjectFile(Project.Type.templates.name(), name);
                    if (f == null) {
                        log.info("Add new template " + name);
                        ProjectFile file = new ProjectFile(name, value, Project.Type.templates.name(), Instant.now().toEpochMilli());
                        karavanCache.saveProjectFile(file, false, true);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error during templates project creation", e);
        }
    }

    void addConfigurationProject() {
        try {
            Project configuration = karavanCache.getProject(Project.Type.configuration.name());
            if (configuration == null) {
                log.info("Add configuration project");
                configuration = new Project(Project.Type.configuration.name(), "Configuration", "", Instant.now().toEpochMilli(), Project.Type.configuration);
                karavanCache.saveProject(configuration, true);

                codeService.getConfigurationFiles().forEach((name, value) -> {
                    ProjectFile file = new ProjectFile(name, value, Project.Type.configuration.name(), Instant.now().toEpochMilli());
                    karavanCache.saveProjectFile(file, false, true);
                });
            } else {
                codeService.getConfigurationFiles().forEach((name, value) -> {
                    ProjectFile f = karavanCache.getProjectFile(Project.Type.configuration.name(), name);
                    if (f == null) {
                        log.info("Add new configuration " + name);
                        ProjectFile file = new ProjectFile(name, value, Project.Type.configuration.name(), Instant.now().toEpochMilli());
                        karavanCache.saveProjectFile(file, false, true);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error during configuration project creation", e);
        }
    }

    void addServicesProject() {
        try {
            Project services = karavanCache.getProject(Project.Type.services.name());
            if (services == null) {
                log.info("Add dev services project");
                services = new Project(Project.Type.services.name(), "Dev services", "", Instant.now().toEpochMilli(), Project.Type.services);
                karavanCache.saveProject(services, true);

                codeService.getDevServicesFiles().forEach((name, value) -> {
                    ProjectFile file = new ProjectFile(name, value, Project.Type.services.name(), Instant.now().toEpochMilli());
                    karavanCache.saveProjectFile(file, false, true);
                });
            } else {
                codeService.getDevServicesFiles().forEach((name, value) -> {
                    ProjectFile f = karavanCache.getProjectFile(Project.Type.services.name(), name);
                    if (f == null) {
                        log.info("Add new service " + name);
                        ProjectFile file = new ProjectFile(name, value, Project.Type.services.name(), Instant.now().toEpochMilli());
                        karavanCache.saveProjectFile(file, false, true);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error during services project creation", e);
        }
    }
}
