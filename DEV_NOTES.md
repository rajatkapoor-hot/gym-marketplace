# Development Notes

## What was changed

1. Removed duplicate JPA auditing configuration
   - `src/main/java/com/gymnetwork/GymMarketplaceApplication.java`
   - Removed `@EnableJpaAuditing` because auditing is already enabled in `src/main/java/com/gymnetwork/common/config/JpaAuditingConfig.java`.
   - This fixed the startup failure caused by duplicate `jpaAuditingHandler` bean registration.

2. Added H2 support for local development and tests
   - `pom.xml`
     - Added `com.h2database:h2` as a runtime dependency.
   - `src/test/resources/application-test.yml`
     - Added H2 in-memory datasource configuration.
     - Disabled Flyway for test profile and enabled `ddl-auto: create-drop`.

3. Simplified integration test setup
   - `src/test/java/com/gymnetwork/BaseIntegrationTest.java`
     - Removed Testcontainers PostgreSQL dependency and Docker requirement.
     - Kept the test profile active so tests use `application-test.yml`.

## Why these changes were made

- The application failed to start because `@EnableJpaAuditing` was declared twice.
- A local dev/test environment should not rely on Docker/Testcontainers by default, especially when the workspace may not have a valid Docker environment.
- H2 is a lightweight embedded database that is suitable for fast local development and CI-friendly tests.

## Current status

- The application startup bean conflict is resolved.
- Test environment was switched to H2, and a Mockito startup workaround was added for this environment.
- This prevents the inline ByteBuddy mock maker from attempting self-attachment in the current JDK/OS.

## Additional fix

- Added `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` to force Mockito default mock maker.

## Next recommended steps

- Resolve the Mockito inline mock maker failure by either:
  - forcing a compatible Mockito mock maker implementation in `pom.xml`, or
  - setting `-Dnet.bytebuddy.agent.attacher.dump=/tmp/bytebuddy-dump.log` for debugging,
  - or updating to a compatible JDK/Mockito version.
- Add a dedicated `application-dev.yml` if needed for local dev settings separate from `application.yml`.
- Optionally add a `docker-compose` profile for Postgres and Redis if the project needs a full local stack.
