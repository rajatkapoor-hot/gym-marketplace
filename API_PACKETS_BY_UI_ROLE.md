# API Request and Response Packets by UI Role

Base URL in dev: `http://localhost:8080`

Most endpoints return this wrapper:

```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {},
  "timestamp": "2026-07-26T08:53:40.806Z"
}
```

Paginated endpoints return this inside `data`:

```json
{
  "content": [],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 0,
  "totalPages": 0,
  "last": true,
  "first": true
}
```

Authenticated endpoints require:

```http
Authorization: Bearer <accessToken>
```

Enums used by UI:

```text
Role: ROLE_USER, ROLE_GYM_OWNER, ROLE_ADMIN
BookingStatus: PENDING, CONFIRMED, CANCELLED, COMPLETED
PaymentStatus: CREATED, SUCCESS, FAILED, REFUNDED
PassType: DAILY, WEEKLY, MONTHLY
```

## Shared Auth Packets

### Register User or Gym Owner

`POST /api/v1/auth/register`

Request:

```json
{
  "email": "user@example.com",
  "phoneNumber": "9000000000",
  "password": "Password123",
  "firstName": "Asha",
  "lastName": "Rao",
  "role": "ROLE_USER",
  "businessName": null
}
```

For Gym Owner registration, send `role: "ROLE_GYM_OWNER"` and optional `businessName`.

Response `data`:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "tokenType": "Bearer",
  "userId": "uuid",
  "email": "user@example.com",
  "role": "ROLE_USER"
}
```

### Login

`POST /api/v1/auth/login`

Request:

```json
{
  "email": "user@example.com",
  "password": "Password123"
}
```

Response `data`: same as `AuthResponse` above.

### Refresh Token

`POST /api/v1/auth/refresh`

Request:

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

Response `data`: same as `AuthResponse` above.

### Logout

`POST /api/v1/auth/logout`

Request body: none.

Response `data`: `null`.

### Forgot Password

`POST /api/v1/auth/forgot-password`

Request:

```json
{
  "email": "user@example.com"
}
```

Response `data`: `null`.

### Reset Password

`POST /api/v1/auth/reset-password`

Request:

```json
{
  "token": "reset-token",
  "newPassword": "NewPassword123"
}
```

Response `data`: `null`.

### Send OTP

`POST /api/v1/auth/send-otp`

Request:

```json
{
  "phoneNumber": "9000000000"
}
```

Response `data`: `null`.

### Verify OTP

`POST /api/v1/auth/verify-otp`

Request:

```json
{
  "phoneNumber": "9000000000",
  "otp": "123456"
}
```

Response `data`:

```json
true
```

### Change Password

`POST /api/v1/auth/change-password`

Request:

```json
{
  "currentPassword": "Password123",
  "newPassword": "NewPassword123"
}
```

Response `data`: `null`.

### Current Auth User

`GET /api/v1/auth/me`

Response `data`:

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "phoneNumber": "9000000000",
  "role": "ROLE_USER",
  "firstName": "Asha",
  "lastName": "Rao",
  "status": "ACTIVE",
  "emailVerified": false,
  "phoneVerified": false
}
```

## User UI Packets

### Gym Discovery

`GET /gyms?page=0&size=10`

Response `data.content[]`:

```json
{
  "id": "uuid",
  "name": "Fit Hub",
  "slug": "fit-hub",
  "description": "Premium gym",
  "addressLine1": "MG Road",
  "city": "Bengaluru",
  "state": "KA",
  "pincode": "560001",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "status": "APPROVED",
  "ratingAverage": 4.5,
  "ratingCount": 32,
  "coverImageUrl": "https://example.com/cover.jpg"
}
```

Other discovery endpoints with same `GymResponse` shape:

```text
GET /gyms/search?query=fit&city=Bengaluru&page=0&size=10
GET /gyms/nearby?latitude=12.9716&longitude=77.5946&page=0&size=10
GET /gyms/trending
GET /gyms/top-rated?page=0&size=10
GET /gyms/featured
```

### Gym Detail

`GET /gyms/{id}`

Response `data`:

```json
{
  "gymInfo": { "id": "uuid", "name": "Fit Hub", "city": "Bengaluru" },
  "photos": [
    { "id": "uuid", "imageUrl": "https://example.com/1.jpg", "isCover": true, "displayOrder": 1 }
  ],
  "facilities": [
    { "id": "uuid", "facilityName": "Cardio", "iconName": "heart-pulse", "description": "Cardio zone" }
  ],
  "timings": [
    { "id": "uuid", "dayOfWeek": "MONDAY", "openTime": "06:00:00", "closeTime": "22:00:00", "slotCapacity": 25 }
  ],
  "holidays": [
    { "id": "uuid", "holidayDate": "2026-08-15", "reason": "Independence Day" }
  ],
  "pricing": [
    { "id": "uuid", "passType": "DAILY", "originalPrice": 300.00, "discountedPrice": 250.00, "isActive": true }
  ],
  "badges": ["TOP_RATED"]
}
```

Supporting detail endpoints:

```text
GET /gyms/{id}/photos -> GymImageDto[]
GET /gyms/{id}/facilities -> GymFacilityDto[]
GET /gyms/{id}/timings -> GymTimingDto[]
GET /gyms/{id}/holidays -> GymHolidayDto[]
GET /gyms/{id}/occupancy -> GymOccupancyResponse
```

Occupancy response:

```json
{
  "gymId": "uuid",
  "currentOccupancy": 12,
  "maxCapacity": 50,
  "percentage": 24.0
}
```

### User Profile

`GET /users/profile`

Response `data`:

```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "phoneNumber": "9000000000",
  "firstName": "Asha",
  "lastName": "Rao",
  "profilePictureUrl": "https://example.com/me.jpg",
  "gender": "FEMALE",
  "dateOfBirth": "1996-01-10",
  "emergencyContactPhone": "9111111111"
}
```

`PUT /users/profile`

Request:

```json
{
  "firstName": "Asha",
  "lastName": "Rao",
  "profilePictureUrl": "https://example.com/me.jpg",
  "gender": "FEMALE",
  "dateOfBirth": "1996-01-10",
  "emergencyContactPhone": "9111111111"
}
```

Response `data`: `UserProfileResponse`.

Other user profile/favourite endpoints:

```text
DELETE /users/profile -> data null
GET /users/history -> data List<?> currently service-defined
GET /users/bookings -> data List<?> currently service-defined
GET /users/favourites -> data UUID[]
POST /users/favourites/{gymId} -> data null
DELETE /users/favourites/{gymId} -> data null
```

### Booking

`POST /bookings`

Request:

```json
{
  "gymId": "uuid",
  "bookingDate": "2026-07-27",
  "entryTime": "07:30:00"
}
```

Response `data`:

```json
{
  "id": "uuid",
  "userId": "uuid",
  "gymId": "uuid",
  "bookingDate": "2026-07-27",
  "entryTime": "07:30:00",
  "exitTime": null,
  "status": "CONFIRMED",
  "amount": 250.00
}
```

Other booking endpoints:

```text
GET /bookings/{id} -> BookingResponse
GET /bookings/user?page=0&size=10 -> PageResponse<BookingResponse>
POST /bookings/{id}/cancel -> data null
```

### Check-In

`POST /checkin`

Request:

```json
{
  "bookingId": "uuid",
  "qrData": "encrypted-qr-payload"
}
```

Response `data`:

```json
{
  "id": "uuid",
  "userId": "uuid",
  "gymId": "uuid",
  "bookingId": "uuid",
  "checkInTime": "2026-07-26T14:30:00",
  "status": "COMPLETED"
}
```

`GET /checkin/user?page=0&size=10` -> `PageResponse<CheckInResponse>`.

### Wallet

`GET /wallet`

Response `data`:

```json
{
  "walletId": "uuid",
  "userId": "uuid",
  "balance": 1000.00,
  "currency": "INR",
  "status": "ACTIVE"
}
```

`POST /wallet/recharge`

Request:

```json
{
  "amount": 500.00,
  "paymentReferenceId": "razorpay-payment-id"
}
```

Response `data`: `WalletResponse`.

`POST /wallet/refund`

Request:

```json
{
  "amount": 250.00,
  "reason": "Booking cancelled",
  "bookingId": "uuid"
}
```

Response `data`: `WalletResponse`.

Other wallet endpoints:

```text
GET /wallet/history?page=0&size=10 -> PageResponse<WalletLedgerDto>
GET /wallet/transactions?page=0&size=10 -> PageResponse<WalletLedgerDto>
GET /wallet/invoice/{transactionId} -> InvoiceResponse
```

Wallet ledger item:

```json
{
  "id": "uuid",
  "walletId": "uuid",
  "referenceId": "booking-or-payment-reference",
  "type": "CREDIT",
  "category": "RECHARGE",
  "amount": 500.00,
  "balanceAfter": 1500.00,
  "description": "Wallet recharge",
  "createdAt": "2026-07-26T08:53:40Z"
}
```

Invoice response:

```json
{
  "transactionId": "uuid",
  "walletId": "uuid",
  "invoiceNumber": "INV-001",
  "date": "2026-07-26T08:53:40Z",
  "transactionType": "CREDIT",
  "category": "RECHARGE",
  "amount": 500.00,
  "description": "Wallet recharge",
  "userDetails": {
    "name": "Asha Rao",
    "email": "user@example.com"
  }
}
```

### Payment

`POST /payments/create-order`

Request:

```json
{
  "amount": 500.00,
  "currency": "INR"
}
```

Response `data`:

```json
{
  "paymentId": "uuid",
  "razorpayOrderId": "order_xxx",
  "amount": 500.00,
  "currency": "INR"
}
```

`POST /payments/verify`

Request:

```json
{
  "razorpayOrderId": "order_xxx",
  "razorpayPaymentId": "pay_xxx",
  "razorpaySignature": "signature"
}
```

Response `data`:

```json
{
  "id": "uuid",
  "razorpayOrderId": "order_xxx",
  "razorpayPaymentId": "pay_xxx",
  "amount": 500.00,
  "currency": "INR",
  "status": "SUCCESS"
}
```

Other payment endpoints:

```text
GET /payments/history?page=0&size=10 -> PageResponse<PaymentResponse>
POST /payments/webhook -> no ApiResponse wrapper, returns HTTP 200 empty body
```

### Reviews

`POST /reviews`

Request:

```json
{
  "gymId": "uuid",
  "rating": 5,
  "comment": "Great equipment and clean changing rooms."
}
```

Response `data`:

```json
{
  "id": "uuid",
  "userId": "uuid",
  "userName": "Asha Rao",
  "gymId": "uuid",
  "rating": 5,
  "comment": "Great equipment and clean changing rooms.",
  "createdAt": "2026-07-26T08:53:40Z"
}
```

`GET /reviews/gym/{gymId}?page=0&size=10` -> `PageResponse<ReviewResponse>`.

### Notifications

```text
GET /notifications?page=0&size=20 -> PageResponse<NotificationResponse>
GET /notifications/unread-count -> Long
PUT /notifications/{id}/read -> data null
PUT /notifications/read-all -> data null
```

Notification item:

```json
{
  "id": "uuid",
  "title": "Booking confirmed",
  "message": "Your booking is confirmed.",
  "type": "BOOKING",
  "isRead": false,
  "createdAt": "2026-07-26T08:53:40Z"
}
```

## Gym Owner UI Packets

Gym Owner uses shared auth with `role: ROLE_GYM_OWNER`.

### Owner Booking List

`GET /bookings/gym/{gymId}?page=0&size=10`

Response `data`: `PageResponse<BookingResponse>`.

### Owner Check-In History

`GET /checkin/gym/{gymId}?page=0&size=10`

Response `data`: `PageResponse<CheckInResponse>`.

### Gym Analytics

`GET /analytics/gym/{gymId}`

Response `data`:

```json
{
  "gymId": "uuid",
  "totalCheckIns": 120,
  "totalEarnings": 24000.00,
  "totalReviews": 32,
  "averageRating": 4.5
}
```

### Settlements

`GET /settlements/owner?page=0&size=20`

Response `data.content[]`:

```json
{
  "id": "uuid",
  "gymId": "uuid",
  "checkInId": "uuid",
  "bookingAmount": 250.00,
  "platformFee": 25.00,
  "netAmount": 225.00,
  "status": "PENDING",
  "createdAt": "2026-07-26T08:53:40Z"
}
```

### QR Management

`GET /qr/gym/{gymId}`

Response `data`:

```json
{
  "gymId": "uuid",
  "encryptedPayload": "encrypted-qr-payload",
  "qrCodeDataUrl": "data:image/png;base64,..."
}
```

`POST /qr/regenerate`

Request:

```json
{
  "gymId": "uuid"
}
```

Response `data`: `QrResponse`.

`GET /qr/download?gymId={gymId}`

Response: `image/png` bytes, no `ApiResponse` wrapper.

## Admin UI Packets

Admin uses shared auth with `role: ROLE_ADMIN`.

### Admin Gym List

`GET /admin/gyms?page=0&size=20`

Response `data`: `PageResponse<GymResponse>`.

### Admin Access to Owner Operational Views

Admin is also allowed by controller rules to call:

```text
GET /bookings/gym/{gymId}?page=0&size=10
GET /checkin/gym/{gymId}?page=0&size=10
GET /analytics/gym/{gymId}
```

The response packets are the same as Gym Owner packets above.

## Current Gaps for UI Planning

These are not implemented as request packets in the current controllers:

```text
Gym Owner create/update gym profile
Gym Owner upload/manage photos, facilities, timings, holidays, pricing
Admin approve/reject gym
Admin suspend/activate user
Admin list all users
Admin settlement approval/update
```

The UI can display/read many gym objects, but management write APIs for Gym Owner/Admin are not present yet.
