package com.gymnetwork.booking.controller;

import com.gymnetwork.auth.security.UserPrincipal;
import com.gymnetwork.booking.dto.request.BookingRequest;
import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.booking.service.BookingService;
import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "User Booking APIs")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", bookingService.createBooking(principal.getId(), request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBooking(principal.getId(), id)));
    }

    @GetMapping("/user")
    @Operation(summary = "Get user's bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getUserBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getUserBookings(principal.getId(), PageRequest.of(page, size))));
    }

    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Get gym's bookings (Owner/Admin)")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getGymBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getGymBookings(principal.getId(), gymId, PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        bookingService.cancelBooking(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
}
