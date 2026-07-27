# Backend Readiness Tracker

Generated on: 2026-07-27
Working branch: `experiments/backend-readiness-tracker`

Overall status: Partially ready for demo, not yet fully production-ready.

The backend has the main Spring Boot module structure, controllers, services, repositories, Flyway migration files, and JWT/QR/payment/wallet modules in place. Current code review shows some production-oriented pieces already exist, but demo readiness is blocked until the QR check-in path uses the encrypted QR service consistently and the build can be verified in an environment that can resolve Maven Central dependencies.

## Latest verification summary
- Branch setup: created `experiments/backend-readiness-tracker` for experiment work.
- Build check attempted on 2026-07-27: `mvn -q -DskipTests compile` could not resolve `org.springframework.boot:spring-boot-starter-parent:pom:3.2.5` because Maven Central returned HTTP 403 in this environment.
- Runtime check: not rerun after the blocked build check.
- Code review highlights:
  - QR generation/decryption exists in `QrServiceImpl` using AES/GCM.
  - Check-in still has a local simulated QR decrypt path that treats QR data as a raw gym UUID, so the generated encrypted QR payload is not yet wired into check-in.
  - Flyway migration files exist under `src/main/resources/db/migration`, but profile-level migration behavior still needs verification from a clean database.

## Priority 1 — Must-fix before demo
These are the items most likely to break the demo experience or create a poor first impression.

### 1. Authentication flow hardening
- [ ] Ensure registration/login/refresh/logout all work end-to-end with real token validation.
- [ ] Replace any placeholder or mock behavior in password reset and OTP flows with a clearly documented dev-safe path.
- [ ] Verify that unauthorized access returns consistent error responses.
- [ ] Test login and refresh flows with valid and invalid credentials.
- [ ] Confirm token store behavior for the selected environment profile, including Redis-backed and in-memory fallback behavior.

### 2. QR verification and check-in flow
- [ ] Replace the simulated QR validation in `CheckInServiceImpl` with `QrInternalService.decryptGymId(...)` or an equivalent real QR validation flow.
- [ ] Ensure QR check-in validates the correct gym and booking before allowing wallet deduction.
- [x] Prevent duplicate check-ins for the same booking in service logic.
- [ ] Add explicit error responses for invalid, expired, or tampered QR data.
- [ ] Decide whether QR payloads need expiry, gym status checks, or rotating secrets before demo.

### 3. Booking and day-pass purchase flow
- [ ] Validate that a user can create a booking only for a valid gym and active daily pass pricing.
- [ ] Ensure booking creation and cancellation logic are consistent with wallet and check-in behavior.
- [ ] Confirm that booking state transitions are clear and predictable for the UI.
- [ ] Add or verify tests for same-day booking, future booking, cancellation, and completed booking paths.

### 4. Wallet and payment flow reliability
- [ ] Verify wallet creation during registration works reliably.
- [ ] Ensure wallet deduction on successful check-in is atomic and does not leave inconsistent state.
- [ ] Add idempotency protection for payment verification and webhook processing.
- [ ] Confirm failed payments and invalid signatures return clear user-facing errors.
- [ ] Verify ledger entries reconcile with wallet balance for recharge, refund, and check-in deduction flows.

### 5. Error handling and API consistency
- [ ] Validate that all critical endpoints return consistent `ApiResponse` payloads.
- [ ] Ensure validation failures, authentication failures, and business-rule failures are handled uniformly.
- [ ] Review exception messages for clarity and user friendliness.
- [ ] Confirm controllers do not leak internal stack traces, secrets, or payment-provider details.

### 6. Database readiness and migration safety
- [x] Add Flyway migration files for the schema path.
- [ ] Enable and verify Flyway in the main development path instead of relying on Hibernate `ddl-auto: update`.
- [ ] Verify migration scripts run successfully from a clean database.
- [ ] Ensure the schema and seed data are consistent across dev, UAT, and prod profiles.
- [ ] Add a repeatable local command or Docker-based smoke test for migration validation.

## Priority 2 — Should-fix before broader rollout
These items are important for reliability, but may be acceptable for a limited demo if the core flow works.

### 7. Admin APIs
- [ ] Review admin endpoints for required permissions and business rules.
- [ ] Ensure admin operations are covered by authorization checks and audit logging.
- [ ] Add basic admin tests for critical operations.

### 8. Logging and observability
- [ ] Add structured request/response logging for critical flows such as auth, booking, payment, and check-in.
- [ ] Add correlation IDs or request IDs for easier debugging.
- [ ] Ensure sensitive data is not logged.

### 9. API documentation completeness
- [ ] Review OpenAPI docs for all major endpoints and response schemas.
- [ ] Ensure examples and descriptions are present for key flows like auth, booking, payment, and QR check-in.

## Priority 3 — Nice-to-have improvements
These are valuable enhancements but not blockers for a first demo.

### 10. Advanced payment and wallet experience
- [ ] Add refund workflow polish and stronger transaction reconciliation.
- [ ] Support wallet statements with richer metadata and filtering.
- [ ] Add support for more payment states and webhook event handling.

### 11. Gym and discovery enhancements
- [ ] Improve search ranking, filters, and location-based relevance.
- [ ] Add richer gym metadata such as occupancy, reviews, and availability signals.

### 12. Test coverage and hardening
- [ ] Add integration tests for registration, login, booking, check-in, wallet recharge, and payment verification.
- [ ] Add smoke tests for Docker and environment-based startup.
- [ ] Add contract tests for API responses and error payloads.

## Suggested implementation order
1. Wire check-in QR validation to the encrypted QR service and remove the local simulation.
2. Restore reliable Maven dependency resolution in the build environment and rerun compile/tests.
3. Validate auth and booking flows end-to-end.
4. Ensure wallet and payment state handling is safe and consistent.
5. Verify Flyway-based schema management from a clean database.
6. Add integration tests and polish API documentation.
