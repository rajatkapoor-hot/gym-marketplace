# Backend Readiness Tracker

Generated on: 2026-07-26

Overall status: Partially ready for demo, not yet fully production-ready

The backend already has the main module structure, compiles successfully, and responds at runtime. The remaining work is mostly about making the critical user flows reliable, non-mock, and demo-safe.

## Verification summary
- Build check: `mvn -q -DskipTests compile` completed successfully.
- Runtime check: `curl http://localhost:8080/actuator/health` returned `{"status":"UP"}`.

## Priority 1 — Must-fix before demo
These are the items most likely to break the demo experience or create a poor first impression.

### 1. Authentication flow hardening
- [ ] Ensure registration/login/refresh/logout all work end-to-end with real token validation.
- [ ] Replace any placeholder or mock behavior in password reset and OTP flows with a clearly documented dev-safe path.
- [ ] Verify that unauthorized access returns consistent error responses.
- [ ] Test login and refresh flows with valid and invalid credentials.

### 2. QR verification and check-in flow
- [ ] Replace the current simulated QR validation in the check-in service with the real QR decrypt/validate flow.
- [ ] Ensure QR check-in validates the correct gym and booking before allowing wallet deduction.
- [ ] Prevent duplicate check-ins for the same booking.
- [ ] Add explicit error responses for invalid, expired, or tampered QR data.

### 3. Booking and day-pass purchase flow
- [ ] Validate that a user can create a booking only for a valid gym and active daily pass pricing.
- [ ] Ensure booking creation and cancellation logic are consistent with wallet and check-in behavior.
- [ ] Confirm that booking state transitions are clear and predictable for the UI.

### 4. Wallet and payment flow reliability
- [ ] Verify wallet creation during registration works reliably.
- [ ] Ensure wallet deduction on successful check-in is atomic and does not leave inconsistent state.
- [ ] Add idempotency protection for payment verification and webhook processing.
- [ ] Confirm failed payments and invalid signatures return clear user-facing errors.

### 5. Error handling and API consistency
- [ ] Validate that all critical endpoints return consistent `ApiResponse` payloads.
- [ ] Ensure validation failures, authentication failures, and business-rule failures are handled uniformly.
- [ ] Review exception messages for clarity and user friendliness.

### 6. Database readiness and migration safety
- [ ] Enable Flyway in the main development path instead of relying on Hibernate `ddl-auto: update`.
- [ ] Verify migration scripts run successfully from a clean database.
- [ ] Ensure the schema and seed data are consistent across dev, UAT, and prod profiles.

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
1. Fix QR verification and check-in flow.
2. Validate auth and booking flows end-to-end.
3. Ensure wallet and payment state handling is safe and consistent.
4. Enable Flyway-based schema management.
5. Add tests and polish documentation.
