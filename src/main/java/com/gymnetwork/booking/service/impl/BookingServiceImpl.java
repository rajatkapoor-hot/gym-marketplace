package com.gymnetwork.booking.service.impl;

import com.gymnetwork.booking.dto.request.BookingRequest;
import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.booking.entity.BookingEntity;
import com.gymnetwork.booking.repository.BookingRepository;
import com.gymnetwork.booking.service.BookingService;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.gym.dto.response.GymDetailResponse;

import com.gymnetwork.gym.service.GymService;
import com.gymnetwork.shared.enums.BookingStatus;
import com.gymnetwork.shared.enums.PassType;
import com.gymnetwork.shared.service.BookingInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService, BookingInternalService {

    private final BookingRepository bookingRepository;
    private final GymService gymService;

    @Override
    @Transactional
    public BookingResponse createBooking(UUID userId, BookingRequest request) {
        if (bookingRepository.existsByUserIdAndGymIdAndBookingDate(userId, request.getGymId(), request.getBookingDate())) {
            throw new BadRequestException("You already have a booking for this gym on the selected date");
        }

        GymDetailResponse gym = gymService.getGymById(request.getGymId());
        
        BigDecimal amount = gym.getPricing().stream()
                .filter(p -> p.getPassType() == PassType.DAILY && Boolean.TRUE.equals(p.getIsActive()))
                .map(p -> p.getDiscountedPrice() != null ? p.getDiscountedPrice() : p.getOriginalPrice())
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Gym does not have an active daily pass pricing"));

        BookingEntity booking = BookingEntity.builder()
                .userId(userId)
                .gymId(request.getGymId())
                .bookingDate(request.getBookingDate())
                .entryTime(request.getEntryTime())
                .amount(amount)
                .status(BookingStatus.PENDING)
                .build();

        booking = bookingRepository.save(booking);
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID userId, UUID bookingId) {
        BookingEntity booking = getBookingEntity(bookingId);
        
        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("Booking does not belong to this user");
        }
        
        return mapToResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getUserBookings(UUID userId, Pageable pageable) {
        Page<BookingEntity> page = bookingRepository.findByUserIdOrderByBookingDateDesc(userId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getGymBookings(UUID gymOwnerId, UUID gymId, Pageable pageable) {
        // Assume authorization check is done at controller level
        Page<BookingEntity> page = bookingRepository.findByGymIdOrderByBookingDateDesc(gymId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public void cancelBooking(UUID userId, UUID bookingId) {
        BookingEntity booking = getBookingEntity(bookingId);
        
        if (!booking.getUserId().equals(userId)) {
            throw new BadRequestException("Booking does not belong to this user");
        }
        
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel booking with status: " + booking.getStatus());
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        
        // In a real app, publish BookingCancelledEvent so Wallet can process refund if it was CONFIRMED.
        // Wait, rule says: "Wallet deduction occurs only after successful QR check-in."
        // So PENDING -> CANCELLED requires no refund.
    }

    @Override
    @Transactional
    public void markBookingAsCompleted(UUID bookingId) {
        BookingEntity booking = getBookingEntity(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Booking must be CONFIRMED to be marked as COMPLETED");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<?> getUserBookings(UUID userId) {
        // Return active/upcoming bookings for user. Just returning empty list for mock.
        // Needs proper mapping and response DTO
        return java.util.Collections.emptyList();
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<?> getUserBookingHistory(UUID userId) {
        // Return completed/cancelled bookings for user.
        return java.util.Collections.emptyList();
    }

    private BookingEntity getBookingEntity(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private BookingResponse mapToResponse(BookingEntity booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .gymId(booking.getGymId())
                .bookingDate(booking.getBookingDate())
                .entryTime(booking.getEntryTime())
                .exitTime(booking.getExitTime())
                .status(booking.getStatus())
                .amount(booking.getAmount())
                .build();
    }
}
