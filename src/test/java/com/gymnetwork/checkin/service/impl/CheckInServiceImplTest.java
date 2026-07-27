package com.gymnetwork.checkin.service.impl;

import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.booking.service.BookingService;
import com.gymnetwork.checkin.dto.request.CheckInRequest;
import com.gymnetwork.checkin.entity.CheckInEntity;
import com.gymnetwork.checkin.repository.CheckInRepository;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.qr.service.impl.QrServiceImpl;
import com.gymnetwork.shared.enums.BookingStatus;
import com.gymnetwork.shared.service.BookingInternalService;
import com.gymnetwork.shared.service.GymInternalService;
import com.gymnetwork.shared.service.WalletInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceImplTest {

    private static final String INVALID_QR_MESSAGE = "Invalid or tampered QR code payload";

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private BookingService bookingService;

    @Mock
    private BookingInternalService bookingInternalService;

    @Mock
    private WalletInternalService walletInternalService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private GymInternalService gymInternalService;

    private CheckInServiceImpl checkInService;
    private QrServiceImpl qrService;

    private UUID userId;
    private UUID bookingId;
    private UUID gymId;

    @BeforeEach
    void setUp() {
        qrService = new QrServiceImpl(gymInternalService);
        ReflectionTestUtils.setField(qrService, "secretKey", "3c9a1e8f2b5d7a4c6e0f2a4b6c8d0e2f");
        checkInService = new CheckInServiceImpl(
                checkInRepository,
                bookingService,
                bookingInternalService,
                walletInternalService,
                eventPublisher,
                qrService
        );

        userId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        gymId = UUID.randomUUID();
    }

    @Test
    void processCheckInAcceptsValidEncryptedQr() {
        when(gymInternalService.existsById(gymId)).thenReturn(true);
        CheckInRequest request = request(qrService.getGymQr(gymId).getEncryptedPayload());
        BookingResponse booking = booking(gymId);
        when(bookingService.getBooking(userId, bookingId)).thenReturn(booking);
        when(checkInRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());
        when(checkInRepository.save(any(CheckInEntity.class))).thenAnswer(invocation -> {
            CheckInEntity checkIn = invocation.getArgument(0);
            checkIn.setId(UUID.randomUUID());
            return checkIn;
        });

        checkInService.processCheckIn(userId, request);

        verify(walletInternalService).deductWallet(
                userId,
                booking.getAmount(),
                booking.getId().toString(),
                "Check-in deduction for booking " + booking.getId()
        );
        verify(bookingInternalService).markBookingAsCompleted(bookingId);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void processCheckInRejectsInvalidBase64Qr() {
        CheckInRequest request = request("not valid base64");
        when(bookingService.getBooking(userId, bookingId)).thenReturn(booking(gymId));
        when(checkInRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInService.processCheckIn(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(INVALID_QR_MESSAGE);

        verifyNoCheckInSideEffects();
    }

    @Test
    void processCheckInRejectsTamperedCiphertext() {
        when(gymInternalService.existsById(gymId)).thenReturn(true);
        String encryptedPayload = qrService.getGymQr(gymId).getEncryptedPayload();
        CheckInRequest request = request(tamper(encryptedPayload));
        when(bookingService.getBooking(userId, bookingId)).thenReturn(booking(gymId));
        when(checkInRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInService.processCheckIn(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(INVALID_QR_MESSAGE);

        verifyNoCheckInSideEffects();
    }

    @Test
    void processCheckInRejectsQrForAnotherGym() {
        UUID otherGymId = UUID.randomUUID();
        when(gymInternalService.existsById(otherGymId)).thenReturn(true);
        CheckInRequest request = request(qrService.getGymQr(otherGymId).getEncryptedPayload());
        when(bookingService.getBooking(userId, bookingId)).thenReturn(booking(gymId));
        when(checkInRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInService.processCheckIn(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("QR code belongs to a different gym");

        verifyNoCheckInSideEffects();
    }

    private CheckInRequest request(String qrData) {
        CheckInRequest request = new CheckInRequest();
        request.setBookingId(bookingId);
        request.setQrData(qrData);
        return request;
    }

    private BookingResponse booking(UUID bookingGymId) {
        return BookingResponse.builder()
                .id(bookingId)
                .userId(userId)
                .gymId(bookingGymId)
                .bookingDate(LocalDate.now())
                .entryTime(LocalTime.now())
                .exitTime(LocalTime.now().plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .amount(BigDecimal.TEN)
                .build();
    }

    private String tamper(String encryptedPayload) {
        char replacement = encryptedPayload.charAt(encryptedPayload.length() - 2) == 'A' ? 'B' : 'A';
        return encryptedPayload.substring(0, encryptedPayload.length() - 2)
                + replacement
                + encryptedPayload.substring(encryptedPayload.length() - 1);
    }

    private void verifyNoCheckInSideEffects() {
        verify(walletInternalService, never()).deductWallet(any(), any(), any(), any());
        verify(checkInRepository, never()).save(any());
        verify(bookingInternalService, never()).markBookingAsCompleted(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
