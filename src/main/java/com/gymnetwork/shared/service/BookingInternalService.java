package com.gymnetwork.shared.service;

import java.util.List;
import java.util.UUID;

public interface BookingInternalService {
    void markBookingAsCompleted(UUID bookingId);
    List<?> getUserBookings(UUID userId);
    List<?> getUserBookingHistory(UUID userId);
}
