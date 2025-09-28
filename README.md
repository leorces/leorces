# Leorces

A process orchestration platform, designed to execute and manage complex business processes with high performance and
reliability.

## 🚀 Overview

Leorces is a comprehensive process orchestration system that provides:

- **Engine** - Execute complex business processes with event-driven architecture
- **Camunda Integration** - Import and execute existing Camunda BPMN processes
- **REST API** - Full REST API for process management and execution
- **PostgreSQL Persistence** - Reliable data storage with automatic compaction
- **Web UI** - User interface for process monitoring and management

## 🏗️ Architecture

### Core Modules

- **`orchestrator/api`** - Service interfaces for orchestrator operations
- **`orchestrator/engine`** - Core workflow execution engine with activity behaviors and event processing
- **`orchestrator/model`** - Domain models and data structures
- **`orchestrator/rest`** - REST API endpoints for process management
- **`orchestrator/rest-client`** - Client library for interacting with the orchestrator
- **`orchestrator/persistence-api`** - Persistence abstraction layer with interfaces for data storage operations
- **`orchestrator/postgres-persistence`** - PostgreSQL-based persistence layer
- **`orchestrator/juel`** - JUEL expression language integration
- **`orchestrator/ui`** - Web-based user interface

### Extensions

- **`extension/camunda-extension`** - Import and execute Camunda BPMN processes

### Examples

- **`example/server-with-camunda-extension-example`** - Complete server setup with Camunda integration
- **`example/client-example`** - Client application examples

## 📋 Prerequisites

- **Java 21** or higher
- **PostgreSQL 12** or higher
- **Gradle 8.0** or higher

## ⚡ Quick Start

```bash
# Fork and clone the repository
git clone https://github.com/your-username/leorces.git
cd leorces
```

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE leorces;
CREATE USER leorces WITH PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE leorces TO leorces;
```

**Or use Docker Compose for quick setup:**

```bash
# Navigate to the example directory
cd example

# Start PostgreSQL with Docker Compose
docker-compose up -d postgres
```

This will start a PostgreSQL container with the same configuration as above.

### 2. Running the Application

#### Running the Server (Orchestrator)

```bash
# Build the project
./gradlew build

# Run the server example
./gradlew :example:server-with-camunda-extension-example:bootRun
```

The orchestrator server will start on `http://localhost:8080`

- Swagger: http://localhost:8080/swagger-ui/index.html#/
- UI: http://localhost:8080/processes

#### Running the Client (Worker)

In a separate terminal, run the client example:

```bash
# Run the client example
./gradlew :example:client-example:bootRun
```

The client worker will start on `http://localhost:8081` and connect to the orchestrator server.

### 3. Health Check

```bash
# Server Health Check
curl http://localhost:8080/actuator/health
```

```bash
# Client Health Check
curl http://localhost:8081/actuator/health
```

## 🔧 Configuration

### Engine Configuration

```yaml
leorces:
  engine:
    compaction:
      enable: true          # Enable automatic data compaction
      batch-size: 2000      # Records per compaction batch
      cron: 0 0 0 * * *     # Daily at midnight
```

### Camunda Extension

```yaml
leorces:
  extension:
    camunda:
      bpmn-path: bpmn                    # BPMN files directory
      processes:
        ProcessName:
          task-retries: 3                # Default task retries
          tasks:
            taskId:
              retries: 5                 # Task-specific retries
```

### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/leorces
    username: leorces
    password: admin123
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000
```

## 📖 Usage Examples

### Starting a Process

```bash
curl --location 'http://localhost:8080/api/v1/runtime/processes/key' \
--header 'accept: */*' \
--header 'Content-Type: application/json' \
--data '{
    "definitionKey": "OrderSubmittedProcess",
    "variables": {
        "order": {
            "number": 1234
        },
        "client": {
            "firstName": "Json",
            "lastName": "Statement"
        }
    }
}'
```

## 🔍 Monitoring

Access monitoring endpoints:

- **Health**: `http://localhost:8080/actuator/health`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`
- **Info**: `http://localhost:8080/actuator/info`

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :orchestrator:engine:test

# Generate test coverage report
./gradlew jacocoTestReport
```

Coverage reports are generated in `build/reports/jacoco/test/html/index.html`

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details on:

- Creating issues and bug reports
- Submitting feature requests
- Making pull requests
- Code style and standards

### Development Setup

1. Fork and clone the repository
2. Set up PostgreSQL database
3. Run `./gradlew build` to build the project
4. Run tests with `./gradlew test`
5. Create a feature branch and make your changes
6. Submit a pull request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🚧 Project Status

Leorces is currently in **version 0.0.1-SNAPSHOT** and under active development.

## 💬 Support

- 📖 [Documentation](https://github.com/leorces/leorces/wiki)
- 🐛 [Issue Tracker](https://github.com/leorces/leorces/issues)
- 💡 [Feature Requests](https://github.com/leorces/leorces/issues/new?template=feature_request.md)

---

⭐ **Star this repository if you find it helpful!**