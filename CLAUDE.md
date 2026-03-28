# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Cloud microservices platform for a programming study/quiz service. Built with Java 21, Gradle multi-module, and Spring Cloud 2025.0.0.

## Build Commands

```bash
# Build all modules
./gradlew build

# Build a specific module
./gradlew :problem:build

# Run all tests
./gradlew test

# Run checkstyle (Naver coding standards)
./gradlew checkstyleMain
./gradlew checkstyleTest

# Run a specific service locally
./gradlew :eureka:bootRun
./gradlew :gateway:bootRun
./gradlew :problem:bootRun
```

## Docker

All services run via Docker Compose. Config files and `.env` are under `docker/`.

```bash
# Start databases (MySQL + Redis)
cd docker && docker-compose -f docker-compose-db.yml up -d

# Start microservices
docker-compose -f docker-compose-was.yml up -d
```

Service YAMLs are in `docker/yml/application-{service}.yml` (not inside each module).

## Module Structure

```
eureka/          - Service discovery (Eureka server, port 8761)
gateway/         - API gateway (routes all /api/v1/** traffic, port 8080)
problem/         - Problem management service
user/            - User management service
chapter/         - Chapter/course structure service
flyway/          - DB migrations only (runs once at startup)
common_core/     - Shared exceptions, error codes
common_web/      - JPA, Redis, Swagger, QueryDSL configs; MySQL driver
common_api/      - Feign client interfaces for inter-service calls
buildSrc/        - Custom Gradle plugins (Groovy)
```

## Gradle Plugins (buildSrc)

Each module applies exactly one of these:
- `base-library` — shared library (JAR only, no bootJar)
- `spring-app` — standalone Spring Boot app
- `spring-cloud-app` — microservice with Spring Cloud dependency management
- `querydsl` — adds QueryDSL annotation processor (applied alongside above)

## Architecture

```
Client → Gateway → [problem-service | user-service | chapter-service]
                        ↑ discovered via Eureka
                   MySQL (3306) + Redis (6379)
```

- Gateway aggregates Swagger docs from all services at `/api-docs/{service}`
- Inter-service calls use Feign clients defined in `common_api`; activate with `@EnableFeignClients(basePackages = "com.example.api")`
- `common_web` is a dependency of all business services (provides JPA, Redis, Swagger, QueryDSL)
- `common_core` is a dependency of `common_web` and `common_api`

## Code Style

Checkstyle enforces **Naver Java coding conventions** (`naver-checkstyle-rules.xml`). Apply the IntelliJ formatter from `naver-intellij-formatter.xml` before committing.

## Key Versions (gradle.properties)

- Java: 21
- Spring Cloud BOM: 2025.0.0
- QueryDSL: 5.1.0
- SpringDoc OpenAPI: 2.8.16
