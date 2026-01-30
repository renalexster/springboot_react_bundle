# Getting Started

This project has been refactored into a multi-module Gradle build with separate modules for the backend (Spring Boot) and the frontend (React via Vite).

## Modules
- backend: Spring Boot application (moved from the original root project)
- ui: React application built with Vite; managed by Gradle via the Node plugin

## Common tasks
- Build everything: `./gradlew build`
- Backend only: `./gradlew :backend:bootRun` (run), `./gradlew :backend:test` (tests)
- UI only: `./gradlew :ui:dev` (start dev server), `./gradlew :ui:build` (production build)

Outputs:
- Backend jar: `backend/build/libs`
- UI build: `ui/dist`

Note: The UI dev server runs independently; for integration you can set up a proxy in Vite or serve static assets from Spring Boot by copying `ui/dist` under `backend/src/main/resources/static` during CI/deploy.

---

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.2/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.2/gradle-plugin/packaging-oci-image.html)
* [Resilience4J](https://docs.spring.io/spring-cloud-circuitbreaker/reference/spring-cloud-circuitbreaker-resilience4j.html)
* [Spring Data JDBC](https://docs.spring.io/spring-boot/4.0.2/reference/data/sql.html#data.sql.jdbc)
* [Flyway Migration](https://docs.spring.io/spring-boot/4.0.2/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway)
* [OAuth2 Resource Server](https://docs.spring.io/spring-boot/4.0.2/reference/web/spring-security.html#web.security.oauth2.server)
* [Spring Security](https://docs.spring.io/spring-boot/4.0.2/reference/web/spring-security.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.2/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Using Spring Data JDBC](https://github.com/spring-projects/spring-data-examples/tree/main/jdbc/basics)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

