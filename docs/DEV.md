## Project structure
1. karavan-generator  
Generate Camel Models and Api from Camel sources to Typescript in karavan-core
2. karavan-core  
Front-end Camel Models and Api
3. karavan-Designer  
KaravanDesigner UI component
4. karavan-app
Karavan Application to be installed into Kubernetes
5. karavan-vscode  
VS Code extension based on Karavan Designer

## How to build Karavan Web Application
1. Generate Camel Models and API for Typescript
```
mvn clean compile exec:java -Dexec.mainClass="org.apache.camel.karavan.generator.KaravanGenerator" -f karavan-generator
```

2. Install Karavan core library
```
cd  karavan-core
npm install
```

3. Build Karavan app  
```
mvn clean package -f karavan-app -Dquarkus.profile=public 
```

## How to build Karavan VS Code extension
1. Generate Camel Models and API for Typescript
```
mvn clean compile exec:java -Dexec.mainClass="org.apache.camel.karavan.generator.KaravanGenerator" -f karavan-generator
```

2. Install Karavan core library
```
cd  karavan-core
npm install
```

3. Build Karavan VS Code extension  
```
cd karavan-vscode
npm update && npm install 
npm install -g @vscode/vsce
vsce package
```

## To run karavan-app in the local machine for debugging

#### Prerequisites
- Docker Engine 24+
- Maven 3.9+
- Node.js 22.14.0+

#### Local Development Setup

The project includes a pre-configured `application-local.properties` file for local development with Docker infrastructure.

1. **Configure Docker Network and Hosts**

   Create Docker network:
   ```bash
   docker network create karavan
   ```

   Update `/etc/hosts` (Linux/macOS) or `C:\Windows\System32\drivers\etc\hosts` (Windows):
   ```
   127.0.0.1   gitea      # Karavan local Git server
   127.0.0.1   registry   # Karavan local image registry server
   ```

2. **Start Required Docker Services**

   The local profile uses Docker infrastructure with:
   - **Gitea**: Local Git repository server
   - **Registry**: Local container image registry (port 5000)
   - **Keycloak**: Authentication server (optional, port 8079)

   Start services using docker-compose:
   ```bash
   cd docs/install/karavan-docker
   docker-compose -f docker-compose-local.yaml up -d
   ```

3. **Run Karavan in Quarkus Dev Mode**

   ```bash
   mvn clean compile quarkus:dev -Dquarkus.profile=local,public
   ```

   The application will:
   - Run on `http://localhost:8080`
   - Use **Docker infrastructure** (not Kubernetes)
   - Connect to local Gitea at `http://gitea:3000`
   - Use local registry at `registry:5000`
   - Enable container status monitoring
   - Run frontend dev server on port 3003

4. **Configuration Details**

   The `local` profile (defined in `application-local.properties`) configures:
   - **Infrastructure**: Docker (forced via profile, prevents Kubernetes detection)
   - **Git**: Local Gitea instance
   - **Registry**: Local Docker registry at `registry:5000`
   - **Monitoring**: Container status and statistics enabled
   - **DevServices**: Kubernetes dev services disabled
   - **Logging**: DEBUG level for Karavan components

5. **Windows-Specific Configuration** (Optional)

   If developing on Windows, update `karavan-app/src/main/webui/package.json` scripts (lines 5-12):
   ```json
   "scripts": {
     "copy-designer": "xcopy ..\\..\\..\\..\\karavan-designer\\src\\designer src\\designer /E/H/Y",
     "copy-knowledgebase": "xcopy ..\\..\\..\\..\\karavan-designer\\src\\knowledgebase src\\knowledgebase /E/H/Y",
     "copy-topology": "xcopy ..\\..\\..\\..\\karavan-designer\\src\\topology src\\topology /E/H/Y",
     "copy-code": "npm run copy-designer && npm run copy-knowledgebase && npm run copy-topology",
     "start": "set PORT=3003 && npm run copy-code && react-scripts start",
     "build": "npm run copy-code && DISABLE_ESLINT_PLUGIN=true react-scripts build"
   }
   ```

#### Troubleshooting

**Error: "Cannot apply manifests because the Kubernetes dev service is not running"**
- **Cause**: Application is trying to use Kubernetes infrastructure instead of Docker
- **Solution**: Ensure you're using the `local` profile: `-Dquarkus.profile=local,public`

**Error: "Connection refused" to Gitea or Registry**
- **Cause**: Docker services not running or hosts file not configured
- **Solution**:
  1. Check services: `docker ps`
  2. Verify hosts file entries
  3. Ensure Docker network exists: `docker network ls | grep karavan`

**Error: Port conflicts**
- **Cause**: Ports 8080, 3003, 3000, or 5000 already in use
- **Solution**: Stop conflicting services or modify ports in `application-local.properties`
