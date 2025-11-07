## Karavan in Docker

### Requirements
1. Linux or MacOS
2. Docker Engine 24+

### How to run Karavan on Docker

There are two deployment modes:

#### 1. Production Mode (Full Docker Deployment)

For running Karavan as a complete Docker-based application:

1. Download [docker-compose.yaml](install/karavan-docker/docker-compose.yaml)
2. Set Environment Variables for Git Repository and Container Image Registry connections in docker-compose.yaml
3. Create Docker network:
   ```bash
   docker network create karavan
   ```
4. Start Karavan:
   ```bash
   docker compose up -d
   ```
5. Open http://localhost:8080

#### 2. Local Development Mode (Docker Infrastructure with Quarkus Dev)

For local development with hot-reload and debugging support:

1. **Prerequisites**:
   - Docker Engine 24+
   - Maven 3.9+
   - Node.js 22.14.0+

2. **Configure Hosts File**:

   Add to `/etc/hosts` (Linux/macOS) or `C:\Windows\System32\drivers\etc\hosts` (Windows):
   ```
   127.0.0.1   gitea      # Karavan local Git server
   127.0.0.1   registry   # Karavan local image registry
   ```

3. **Create Docker Network**:
   ```bash
   docker network create karavan
   ```

4. **Start Docker Infrastructure Services**:
   ```bash
   cd docs/install/karavan-docker
   docker-compose -f docker-compose-local.yaml up -d
   ```

   This starts:
   - **Gitea** (Git server) on port 3000
   - **Registry** (Container registry) on port 5000
   - **Keycloak** (Optional, Authentication) on port 8079

5. **Run Karavan Application in Dev Mode**:
   ```bash
   cd ../../..  # Return to project root
   mvn clean compile quarkus:dev -Dquarkus.profile=local,public -f karavan-app
   ```

6. **Access Application**:
   - **Karavan UI**: http://localhost:8080
   - **Gitea UI**: http://localhost:3000 (admin/karavankaravan)
   - **Registry**: http://localhost:5000

### Configuration

The local development setup uses `application-local.properties` which configures:
- **Infrastructure**: Docker (not Kubernetes)
- **Git Repository**: Local Gitea instance
- **Container Registry**: Local Docker registry
- **Container Monitoring**: Enabled for Docker
- **Hot Reload**: Frontend dev server on port 3003

### Troubleshooting

**"Cannot apply manifests because the Kubernetes dev service is not running"**
- Ensure you're using the `local` profile: `-Dquarkus.profile=local,public`
- The local profile forces Docker infrastructure instead of Kubernetes

**Connection errors to Gitea or Registry**
- Verify Docker services are running: `docker ps`
- Check hosts file configuration
- Ensure Docker network exists: `docker network ls | grep karavan`

**Port conflicts**
- Default ports: 8080 (Karavan), 3000 (Gitea), 5000 (Registry), 3003 (Frontend Dev)
- Stop conflicting services or modify ports in `docker-compose-local.yaml`

For detailed development setup, see [DEV.md](DEV.md)
