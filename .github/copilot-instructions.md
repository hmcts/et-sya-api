# Copilot Instructions for et-sya-api

## Project Overview
- **et-sya-api** is a Spring Boot (v3+) Java backend for Employment Tribunal self-service applications.
- The project is structured for modularity: main business logic is in `src/main/java`, with tests in `src/test/java`, `src/integrationTest/java`, and `src/functionalTest/java`.
- Integration with external services (CCD, IDAM, Notification, Bundles) is via autowired service beans and REST clients, all of which are mocked in integration tests.

## Build & Test Workflows
- **Build:** Use `./gradlew build` (wrapper included, no need to install Gradle).
- **Run Locally:**
  - Standard: `./gradlew bootRun`
  - With cftlib profile: `./gradlew bootRun --args='--spring.profiles.active=cftlib'`
  - Docker: `./bin/run-in-docker.sh` (see script help for options)
- **Test:**
  - Unit & integration: `./gradlew test`
  - Functional: `./gradlew functional` (requires environment variables, see README)
  - Contract/Pact: `./gradlew contract` and `./gradlew pactPublish`
- **Health Check:** `curl http://localhost:4550/health`
- **Swagger UI:** `http://localhost:4550/swagger-ui/index.html`

## Key Patterns & Conventions
- **Testing:**
  - JUnit 5 is required (no JUnit 4).
  - Integration tests use `@SpringBootTest` and `@AutoConfigureMockMvc(addFilters = false)`.
  - All external dependencies are mocked with `@MockBean`.
  - Test data is loaded from JSON files in `src/test/resources` and similar.
  - For Spring Boot 3, always mock CCD client responses to return non-null lists (see `ManageCaseControllerIntegrationTest`).
- **Configuration:**
  - Profiles: `application.yaml` (default), `application-cftlib.yaml` (cftlib profile).
  - Bean overriding is disabled by default; enable with `spring.main.allow-bean-definition-overriding=true` if needed.
- **External Integrations:**
  - CCD, IDAM, Notification, Bundles: all via REST clients, with service boundaries clear in `service/` package.
  - Infrastructure as code is in `/infrastructure` (Terraform).
- **API Design:**
  - REST endpoints are in `controllers/`.
  - DTOs are in `models/`.
  - Constants in `constants/`.

## Examples
- See `src/integrationTest/java/uk/gov/hmcts/reform/et/syaapi/controllers/ManageCaseControllerIntegrationTest.java` for integration test structure and mocking patterns.
- See `README.md` for full build, run, and test instructions.

## Special Notes
- Functional tests may require VPN and specific environment variables for AAT.
- For local functional tests, user credentials may need to be updated in the test code.
- Use the health endpoint and Swagger UI for quick diagnostics.

---

For more, see `README.md` and `infrastructure/README.md`. Update this file if project structure or conventions change.
