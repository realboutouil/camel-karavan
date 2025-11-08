# Apache Camel Karavan - Development Makefile
# ============================================

.PHONY: help all clean install build test dev docker-up docker-down

# Default target
.DEFAULT_GOAL := help

# Colors for output
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

# Variables
MAVEN := mvn
YARN := yarn
DOCKER_COMPOSE := docker-compose
GENERATOR_CLASS := org.apache.camel.karavan.generator.KaravanGenerator

##@ General

help: ## Display this help message
	@echo "$(BLUE)Apache Camel Karavan - Development Commands$(NC)"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make $(CYAN)<target>$(NC)\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  $(CYAN)%-25s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(YELLOW)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

all: clean install generate build ## Clean, install dependencies, generate code, and build all projects

##@ Code Generation

generate: ## Generate Camel Models and API from Camel sources
	@echo "$(GREEN)Generating Camel Models and API...$(NC)"
	$(MAVEN) clean compile exec:java -Dexec.mainClass="$(GENERATOR_CLASS)" -f karavan-generator

generate-debug: ## Generate with Maven debug output
	@echo "$(GREEN)Generating Camel Models and API (debug mode)...$(NC)"
	$(MAVEN) clean compile exec:java -Dexec.mainClass="$(GENERATOR_CLASS)" -f karavan-generator -X

##@ Dependencies

install: install-core install-designer install-app-webui ## Install dependencies for all yarn projects

install-core: ## Install karavan-core dependencies
	@echo "$(GREEN)Installing karavan-core dependencies...$(NC)"
	cd karavan-core && $(YARN) install

install-designer: ## Install karavan-designer dependencies
	@echo "$(GREEN)Installing karavan-designer dependencies...$(NC)"
	cd karavan-designer && $(YARN) install

install-app-webui: ## Install karavan-app webui dependencies
	@echo "$(GREEN)Installing karavan-app webui dependencies...$(NC)"
	cd karavan-app/src/main/webui && $(YARN) install

##@ Build

build: build-core build-designer build-app ## Build all projects

build-core: ## Build karavan-core library
	@echo "$(GREEN)Building karavan-core...$(NC)"
	cd karavan-core && $(YARN) build

build-designer: ## Build karavan-designer standalone application
	@echo "$(GREEN)Building karavan-designer...$(NC)"
	cd karavan-designer && $(YARN) build

build-app: ## Build karavan-app (Quarkus application)
	@echo "$(GREEN)Building karavan-app...$(NC)"
	$(MAVEN) clean package -f karavan-app -Dquarkus.profile=public

build-app-fast: ## Build karavan-app (fast mode, skip tests)
	@echo "$(GREEN)Building karavan-app (fast mode)...$(NC)"
	$(MAVEN) clean package -f karavan-app -Dquarkus.profile=public -DskipTests

build-generator: ## Build karavan-generator
	@echo "$(GREEN)Building karavan-generator...$(NC)"
	$(MAVEN) clean package -f karavan-generator

##@ Development

dev-setup: ## Setup local Docker infrastructure (Gitea, Registry)
	@echo "$(GREEN)Setting up local Docker infrastructure...$(NC)"
	@echo "$(YELLOW)Creating Docker network 'karavan'...$(NC)"
	-docker network create karavan
	@echo "$(YELLOW)Starting Docker services (Gitea, Registry)...$(NC)"
	cd docs/install/karavan-docker && $(DOCKER_COMPOSE) -f docker-compose-local.yaml up -d
	@echo "$(GREEN)Local infrastructure is ready!$(NC)"
	@echo "$(BLUE)Gitea:    http://localhost:3000 (admin/karavankaravan)$(NC)"
	@echo "$(BLUE)Registry: http://localhost:5000$(NC)"
	@echo ""
	@echo "$(YELLOW)IMPORTANT: Add to /etc/hosts:$(NC)"
	@echo "127.0.0.1   gitea"
	@echo "127.0.0.1   registry"

dev-app: ## Run karavan-app in Quarkus dev mode (local profile with Docker infrastructure)
	@echo "$(GREEN)Starting karavan-app in dev mode (Docker infrastructure)...$(NC)"
	@echo "$(YELLOW)Using profile: local,public$(NC)"
	$(MAVEN) clean compile quarkus:dev -f karavan-app -Dquarkus.profile=local,public

dev-core: ## Run karavan-core in dev mode
	@echo "$(GREEN)Starting karavan-core in dev mode...$(NC)"
	cd karavan-core && $(YARN) dev

dev-designer: ## Run karavan-designer in dev mode
	@echo "$(GREEN)Starting karavan-designer in dev mode...$(NC)"
	cd karavan-designer && $(YARN) dev

##@ Testing

test: test-core ## Run all tests

test-core: ## Run karavan-core tests
	@echo "$(GREEN)Running karavan-core tests...$(NC)"
	cd karavan-core && $(YARN) test

test-app: ## Run karavan-app tests
	@echo "$(GREEN)Running karavan-app tests...$(NC)"
	$(MAVEN) test -f karavan-app

test-generator: ## Run karavan-generator tests
	@echo "$(GREEN)Running karavan-generator tests...$(NC)"
	$(MAVEN) test -f karavan-generator

##@ Docker / Container Images

docker-build: docker-build-app docker-build-app-oidc docker-build-devmode ## Build all container images (app, app-oidc, devmode)

docker-build-app: ## Build karavan-app container image with Jib
	@echo "$(GREEN)Building karavan-app container image with Jib...$(NC)"
	$(MAVEN) clean package -f karavan-app -Dquarkus.profile=public -DskipTests \
		-Dquarkus.container-image.build=true \
		-Dquarkus.container-image.group=apache \
		-Dquarkus.container-image.name=camel-karavan \
		-Dquarkus.container-image.tag=4.14.2

docker-build-app-oidc: ## Build karavan-app OIDC container image with Jib
	@echo "$(GREEN)Building karavan-app OIDC container image with Jib...$(NC)"
	$(MAVEN) clean package -f karavan-app -Dquarkus.profile=oidc -DskipTests \
		-Dquarkus.container-image.build=true \
		-Dquarkus.container-image.group=apache \
		-Dquarkus.container-image.name=camel-karavan \
		-Dquarkus.container-image.tag=4.14.2-oidc

docker-build-devmode: ## Build karavan-devmode container image with Docker
	@echo "$(GREEN)Building karavan-devmode container image with Docker...$(NC)"
	docker buildx build --platform linux/amd64 \
		-t apache/camel-karavan-devmode:4.14.2 \
		-f karavan-devmode/Dockerfile \
		karavan-devmode

docker-build-native: ## Build native container images with Jib
	@echo "$(GREEN)Building native container images with Jib...$(NC)"
	$(MAVEN) clean package -f karavan-app -Pnative -Dquarkus.profile=public -DskipTests \
		-Dquarkus.container-image.build=true

docker-push: docker-push-app docker-push-app-oidc docker-push-devmode ## Build and push all container images

docker-push-app: ## Build and push karavan-app container image
	@echo "$(GREEN)Building and pushing karavan-app container image...$(NC)"
	$(MAVEN) clean package -f karavan-app -Dquarkus.profile=public -DskipTests \
		-Dquarkus.container-image.build=true \
		-Dquarkus.container-image.push=true \
		-Dquarkus.container-image.registry=ghcr.io \
		-Dquarkus.container-image.group=apache \
		-Dquarkus.container-image.name=camel-karavan \
		-Dquarkus.container-image.tag=4.14.2

docker-push-app-oidc: ## Build and push karavan-app OIDC container image
	@echo "$(GREEN)Building and pushing karavan-app OIDC container image...$(NC)"
	$(MAVEN) clean package -f karavan-app -Dquarkus.profile=oidc -DskipTests \
		-Dquarkus.container-image.build=true \
		-Dquarkus.container-image.push=true \
		-Dquarkus.container-image.registry=ghcr.io \
		-Dquarkus.container-image.group=apache \
		-Dquarkus.container-image.name=camel-karavan \
		-Dquarkus.container-image.tag=4.14.2-oidc

docker-push-devmode: ## Build and push karavan-devmode container image
	@echo "$(GREEN)Building and pushing karavan-devmode container image...$(NC)"
	docker buildx build --platform linux/amd64 \
		-t ghcr.io/apache/camel-karavan-devmode:4.14.2 \
		--push \
		-f karavan-devmode/Dockerfile \
		karavan-devmode

docker-up: ## Start Docker services (karavan-docker)
	@echo "$(GREEN)Starting Docker services...$(NC)"
	cd docs/install/karavan-docker && $(DOCKER_COMPOSE) up -d

docker-down: ## Stop Docker services
	@echo "$(GREEN)Stopping Docker services...$(NC)"
	cd docs/install/karavan-docker && $(DOCKER_COMPOSE) down

docker-logs: ## Show Docker logs
	@echo "$(GREEN)Showing Docker logs...$(NC)"
	cd docs/install/karavan-docker && $(DOCKER_COMPOSE) logs -f

docker-restart: docker-down docker-up ## Restart Docker services

docker-local-up: ## Start Docker services with local config
	@echo "$(GREEN)Starting Docker services (local config)...$(NC)"
	cd docs/install/karavan-docker && $(DOCKER_COMPOSE) -f docker-compose-local.yaml up -d

docker-local-down: ## Stop Docker services (local config)
	@echo "$(GREEN)Stopping Docker services (local config)...$(NC)"
	cd docs/install/karavan-docker && $(DOCKER_COMPOSE) -f docker-compose-local.yaml down

##@ Versioning

version: ## Update version (usage: make version VERSION=4.14.2)
	@if [ -z "$(VERSION)" ]; then \
		echo "$(RED)Error: VERSION is required. Usage: make version VERSION=4.14.2$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)Updating version to $(VERSION)...$(NC)"
	./change_version.sh $(VERSION)

##@ Dependencies Update

deps-check: ## Check for outdated yarn dependencies
	@echo "$(GREEN)Checking karavan-core dependencies...$(NC)"
	cd karavan-core && $(YARN) outdated || true
	@echo "$(GREEN)Checking karavan-app webui dependencies...$(NC)"
	cd karavan-app/src/main/webui && $(YARN) outdated || true

deps-update: ## Update yarn dependencies with ncu
	@echo "$(GREEN)Updating dependencies...$(NC)"
	cd karavan-core && ncu -u && $(YARN) install
	cd karavan-app/src/main/webui && ncu -u && $(YARN) install

##@ Linting & Formatting

lint: ## Run ESLint on karavan-app webui
	@echo "$(GREEN)Linting karavan-app webui...$(NC)"
	cd karavan-app/src/main/webui && $(YARN) lint || true

lint-fix: ## Fix ESLint issues automatically
	@echo "$(GREEN)Fixing lint issues in karavan-app webui...$(NC)"
	cd karavan-app/src/main/webui && $(YARN) lint:fix || true

##@ Cleaning

clean: clean-core clean-designer clean-app clean-generator ## Clean all build artifacts

clean-core: ## Clean karavan-core build artifacts
	@echo "$(YELLOW)Cleaning karavan-core...$(NC)"
	cd karavan-core && rm -rf lib node_modules yarn.lock

clean-designer: ## Clean karavan-designer build artifacts
	@echo "$(YELLOW)Cleaning karavan-designer...$(NC)"
	cd karavan-designer && rm -rf dist node_modules yarn.lock

clean-app: ## Clean karavan-app build artifacts
	@echo "$(YELLOW)Cleaning karavan-app...$(NC)"
	$(MAVEN) clean -f karavan-app
	cd karavan-app/src/main/webui && rm -rf node_modules yarn.lock

clean-generator: ## Clean karavan-generator build artifacts
	@echo "$(YELLOW)Cleaning karavan-generator...$(NC)"
	$(MAVEN) clean -f karavan-generator

clean-all: clean ## Deep clean (includes Docker volumes)
	@echo "$(RED)Performing deep clean...$(NC)"
	cd docs/install/karavan-docker && $(DOCKER_COMPOSE) down -v || true

##@ Quick Start

quickstart: generate install-core build-app ## Quick start: generate, install core, and build app

dev-quickstart: generate install ## Quick dev setup: generate code and install all dependencies

##@ CI/CD

ci-build: ## CI build pipeline
	@echo "$(GREEN)Running CI build pipeline...$(NC)"
	$(MAVEN) clean compile exec:java -Dexec.mainClass="$(GENERATOR_CLASS)" -f karavan-generator
	cd karavan-core && $(YARN) install --frozen-lockfile
	$(MAVEN) clean package -f karavan-app -Dquarkus.profile=public -DskipTests

ci-test: ## CI test pipeline
	@echo "$(GREEN)Running CI test pipeline...$(NC)"
	cd karavan-core && $(YARN) test
	$(MAVEN) test -f karavan-app
