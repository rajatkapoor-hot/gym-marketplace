package com.gymnetwork.booking.service.impl;

import com.gymnetwork.booking.dto.request.BookingRequest;
import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.booking.entity.BookingEntity;
import com.gymnetwork.booking.repository.BookingRepository;
import com.gymnetwork.gym.dto.response.GymDetailResponse;
import com.gymnetwork.gym.dto.response.GymPricingDto;
import com.gymnetwork.gym.service.GymService;
import com.gymnetwork.shared.enums.BookingStatus;
import com.gymnetwork.shared.enums.PassType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GymService gymService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void newlyCreatedBookingsAppearInActiveBookings() {
        UUID userId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();
        BookingRequest request = new BookingRequest();
        request.setGymId(gymId);
        request.setBookingDate(LocalDate.now().plusDays(1));
        request.setEntryTime(LocalTime.of(9, 0));

        when(bookingRepository.existsByUserIdAndGymIdAndBookingDate(userId, gymId, request.getBookingDate()))
                .thenReturn(false);
        when(gymService.getGymById(gymId)).thenReturn(GymDetailResponse.builder()
                .pricing(List.of(GymPricingDto.builder()
                        .passType(PassType.DAILY)
                        .originalPrice(BigDecimal.TEN)
                        .isActive(true)
                        .build()))
                .build());
        when(bookingRepository.save(any(BookingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse createdBooking = bookingService.createBooking(userId, request);

        ArgumentCaptor<BookingEntity> savedBookingCaptor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingRepository).save(savedBookingCaptor.capture());
        BookingEntity savedBooking = savedBookingCaptor.getValue();
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(createdBooking.getStatus()).isEqualTo(BookingStatus.PENDING);

        when(bookingRepository.findByUserIdAndStatusInOrderByBookingDateAscEntryTimeAsc(
                userId, List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)))
                .thenReturn(List.of(savedBooking));

        List<BookingResponse> activeBookings = bookingService.getUserBookings(userId);

        assertThat(activeBookings)
                .extracting(BookingResponse::getStatus)
                .containsExactly(BookingStatus.PENDING);
    }

    @Test
    void cancelledAndCompletedBookingsAppearInBookingHistory() {
        UUID userId = UUID.randomUUID();
        BookingEntity cancelledBooking = booking(userId, BookingStatus.CANCELLED, LocalDate.now().minusDays(1));
        BookingEntity completedBooking = booking(userId, BookingStatus.COMPLETED, LocalDate.now().minusDays(2));

        when(bookingRepository.findByUserIdAndStatusInOrderByBookingDateAscEntryTimeAsc(
                userId, List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.EXPIRED)))
                .thenReturn(List.of(completedBooking, cancelledBooking));

        List<BookingResponse> history = bookingService.getUserBookingHistory(userId);

        assertThat(history)
                .extracting(BookingResponse::getStatus)
                .containsExactly(BookingStatus.COMPLETED, BookingStatus.CANCELLED);
    }

    @Test
    void cancellingABookingMovesItToBookingHistoryStatuses() {
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        BookingEntity booking = booking(userId, BookingStatus.PENDING, LocalDate.now().plusDays(1));
        booking.setId(bookingId);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        bookingService.cancelBooking(userId, bookingId);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bookingRepository).save(booking);
    }

    private BookingEntity booking(UUID userId, BookingStatus status, LocalDate bookingDate) {
        return BookingEntity.builder()
                .userId(userId)
                .gymId(UUID.randomUUID())
                .bookingDate(bookingDate)
                .entryTime(LocalTime.of(9, 0))
                .status(status)
                .amount(BigDecimal.TEN)
                .build();
    }
}
