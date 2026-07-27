package com.gymnetwork.booking;

import com.gymnetwork.BaseIntegrationTest;
import com.gymnetwork.booking.dto.request.BookingRequest;
import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.booking.entity.BookingEntity;
import com.gymnetwork.booking.repository.BookingRepository;
import com.gymnetwork.booking.service.BookingService;
import com.gymnetwork.checkin.dto.request.CheckInRequest;
import com.gymnetwork.checkin.dto.response.CheckInResponse;
import com.gymnetwork.checkin.repository.CheckInRepository;
import com.gymnetwork.checkin.service.CheckInService;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.gym.dto.response.GymDetailResponse;
import com.gymnetwork.gym.dto.response.GymPricingDto;
import com.gymnetwork.gym.service.GymService;
import com.gymnetwork.shared.enums.BookingStatus;
import com.gymnetwork.shared.enums.PassType;
import com.gymnetwork.shared.service.BookingInternalService;
import com.gymnetwork.shared.service.WalletInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingCheckInIntegrationTest extends BaseIntegrationTest {

    private static final BigDecimal DAILY_PASS_PRICE = new BigDecimal("199.00");

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private BookingInternalService bookingInternalService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @MockBean
    private GymService gymService;

    @MockBean
    private WalletInternalService walletInternalService;

    @BeforeEach
    void setUp() {
        checkInRepository.deleteAll();
        bookingRepository.deleteAll();
    }

    @Test
    void createBookingCreatesConfirmedDayPassBooking() {
        UUID userId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();
        when(gymService.getGymById(gymId)).thenReturn(gymWithDailyPass());

        BookingResponse response = bookingService.createBooking(userId, bookingRequest(gymId));

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getStatusDescription()).isEqualTo("Confirmed - show your QR at the gym to check in");
        assertThat(response.getAmount()).isEqualByComparingTo(DAILY_PASS_PRICE);
        assertThat(bookingRepository.findById(response.getId()))
                .get()
                .extracting(BookingEntity::getStatus)
                .isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void checkInWithValidQrCompletesBooking() {
        UUID userId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();
        when(gymService.getGymById(gymId)).thenReturn(gymWithDailyPass());
        BookingResponse booking = bookingService.createBooking(userId, bookingRequest(gymId));

        CheckInResponse checkIn = checkInService.processCheckIn(userId, checkInRequest(booking.getId(), gymId));

        assertThat(checkIn.getStatus()).isEqualTo("SUCCESS");
        assertThat(checkIn.getBookingId()).isEqualTo(booking.getId());
        assertThat(bookingRepository.findById(booking.getId()))
                .get()
                .extracting(BookingEntity::getStatus)
                .isEqualTo(BookingStatus.COMPLETED);
        verify(walletInternalService).deductWallet(eq(userId), eq(DAILY_PASS_PRICE), eq(booking.getId().toString()), any(String.class));
    }

    @Test
    void invalidCompletionTransitionReturnsClearBusinessRuleError() {
        BookingEntity cancelledBooking = bookingRepository.save(BookingEntity.builder()
                .userId(UUID.randomUUID())
                .gymId(UUID.randomUUID())
                .bookingDate(LocalDate.now())
                .entryTime(LocalTime.of(10, 0))
                .amount(DAILY_PASS_PRICE)
                .status(BookingStatus.CANCELLED)
                .build());

        assertThatThrownBy(() -> bookingInternalService.markBookingAsCompleted(cancelledBooking.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid booking status transition: only CONFIRMED bookings can be marked as COMPLETED, but booking is CANCELLED");
    }

    private BookingRequest bookingRequest(UUID gymId) {
        BookingRequest request = new BookingRequest();
        request.setGymId(gymId);
        request.setBookingDate(LocalDate.now());
        request.setEntryTime(LocalTime.of(10, 0));
        return request;
    }

    private CheckInRequest checkInRequest(UUID bookingId, UUID gymId) {
        CheckInRequest request = new CheckInRequest();
        request.setBookingId(bookingId);
        request.setQrData(gymId.toString());
        return request;
    }

    private GymDetailResponse gymWithDailyPass() {
        return GymDetailResponse.builder()
                .pricing(List.of(GymPricingDto.builder()
                        .passType(PassType.DAILY)
                        .originalPrice(DAILY_PASS_PRICE)
                        .isActive(true)
                        .build()))
                .build();
    }
}
