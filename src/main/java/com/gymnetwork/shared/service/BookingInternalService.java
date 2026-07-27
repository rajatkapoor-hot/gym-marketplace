package com.gymnetwork.shared.service;

import com.gymnetwork.booking.dto.response.BookingResponse;

import java.util.List;
import java.util.UUID;

public interface BookingInternalService {
    void markBookingAsCompleted(UUID bookingId);
    List<BookingResponse> getUserBookings(UUID userId);
    List<BookingResponse> getUserBookingHistory(UUID userId);
}
