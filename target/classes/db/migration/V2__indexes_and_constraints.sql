-- Flyway Migration V2__indexes_and_constraints.sql

-- Indexing for standard user lookups
CREATE INDEX idx_users_email ON users(email) WHERE deleted = FALSE;
CREATE INDEX idx_users_phone ON users(phone_number) WHERE deleted = FALSE;
CREATE INDEX idx_users_role ON users(role);

-- Indexing for Gym Search and Geo Filtering
CREATE INDEX idx_gyms_status ON gyms(status) WHERE deleted = FALSE;
CREATE INDEX idx_gyms_city ON gyms(city) WHERE deleted = FALSE;
CREATE INDEX idx_gyms_location ON gyms(latitude, longitude) WHERE deleted = FALSE;
CREATE INDEX idx_gyms_owner ON gyms(owner_id);

-- Indexing for Booking Lookup & Verification
CREATE INDEX idx_bookings_user ON bookings(user_id, status);
CREATE INDEX idx_bookings_gym_date ON bookings(gym_id, booking_date, status);
CREATE INDEX idx_bookings_code ON bookings(booking_code);

-- Indexing for Check-in & Review Lookups
CREATE INDEX idx_checkins_booking ON checkins(booking_id);
CREATE INDEX idx_checkins_gym ON checkins(gym_id);
CREATE INDEX idx_reviews_gym ON reviews(gym_id, rating);

-- Indexing for Wallet Ledger Transactions
CREATE INDEX idx_wallet_ledgers_wallet ON wallet_ledgers(wallet_id, created_at DESC);
CREATE INDEX idx_payments_order ON payments(razorpay_order_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
