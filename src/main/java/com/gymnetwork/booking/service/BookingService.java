package com.gymnetwork.booking.service;

import com.gymnetwork.booking.dto.request.BookingRequest;
import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {
    BookingResponse createBooking(UUID userId, BookingRequest request);
    BookingResponse getBooking(UUID userId, UUID bookingId);
    PageResponse<BookingResponse> getUserBookings(UUID userId, Pageable pageable);
    PageResponse<BookingResponse> getGymBookings(UUID gymOwnerId, UUID gymId, Pageable pageable);
    void cancelBooking(UUID userId, UUID bookingId);
}
