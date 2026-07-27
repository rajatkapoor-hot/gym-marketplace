package com.gymnetwork.checkin.service.impl;

import com.gymnetwork.booking.dto.response.BookingResponse;
import com.gymnetwork.booking.service.BookingService;
import com.gymnetwork.checkin.dto.request.CheckInRequest;
import com.gymnetwork.checkin.dto.response.CheckInResponse;
import com.gymnetwork.checkin.entity.CheckInEntity;
import com.gymnetwork.checkin.repository.CheckInRepository;
import com.gymnetwork.checkin.service.CheckInService;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.common.exception.BadRequestException;

import com.gymnetwork.shared.enums.BookingStatus;
import com.gymnetwork.shared.event.CheckInCompletedEvent;
import com.gymnetwork.shared.service.BookingInternalService;
import com.gymnetwork.shared.service.CheckInInternalService;
import com.gymnetwork.shared.service.WalletInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInServiceImpl implements CheckInService, CheckInInternalService {

    private final CheckInRepository checkInRepository;
    private final BookingService bookingService;
    private final BookingInternalService bookingInternalService;
    private final WalletInternalService walletInternalService;
    private final ApplicationEventPublisher eventPublisher;
    // Assume QrService has a method to decrypt or validate QR data and return Gym ID
    // Since we don't have decrypt in QrService yet, we'll simulate it for now.
    // In actual implementation, we'd add decryptQr(String qrData) to QrService.

    @Override
    @Transactional
    public CheckInResponse processCheckIn(UUID userId, CheckInRequest request) {
        // 1. Get the booking
        BookingResponse booking = bookingService.getBooking(userId, request.getBookingId());
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Invalid booking status for check-in: expected CONFIRMED but was " + booking.getStatus());
        }
        
        if (!booking.getBookingDate().equals(LocalDate.now())) {
            throw new BadRequestException("Booking is not valid for today");
        }
        
        if (checkInRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new BadRequestException("Check-in already processed for this booking");
        }
        
        // 2. Validate QR code
        // For now, assume the QR data string is the Gym UUID directly or encrypted.
        // E.g., we'll just check if it contains the Gym UUID as a simple simulation
        // In real impl: UUID scannedGymId = qrService.decryptQr(request.getQrData());
        String decryptedData = decryptSimulate(request.getQrData());
        UUID scannedGymId;
        try {
            scannedGymId = UUID.fromString(decryptedData);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid QR code");
        }
        
        if (!booking.getGymId().equals(scannedGymId)) {
            throw new BadRequestException("QR code belongs to a different gym");
        }
        
        // 3. Deduct Wallet Balance
        walletInternalService.deductWallet(
                userId, 
                booking.getAmount(), 
                booking.getId().toString(), 
                "Check-in deduction for booking " + booking.getId()
        );
        
        // 4. Record CheckIn
        CheckInEntity checkIn = CheckInEntity.builder()
                .userId(userId)
                .gymId(booking.getGymId())
                .bookingId(booking.getId())
                .checkInTime(LocalDateTime.now())
                .status("SUCCESS")
                .build();
                
        checkIn = checkInRepository.save(checkIn);
        
        // 5. Update Booking Status
        bookingInternalService.markBookingAsCompleted(booking.getId());
        
        // 6. Publish Event for Settlement and Notifications
        eventPublisher.publishEvent(CheckInCompletedEvent.builder()
                .checkInId(checkIn.getId())
                .bookingId(booking.getId())
                .userId(userId)
                .gymId(booking.getGymId())
                .amount(booking.getAmount())
                .build());
        
        return mapToResponse(checkIn);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CheckInResponse> getUserCheckIns(UUID userId, Pageable pageable) {
        Page<CheckInEntity> page = checkInRepository.findByUserIdOrderByCheckInTimeDesc(userId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CheckInResponse> getGymCheckIns(UUID gymOwnerId, UUID gymId, Pageable pageable) {
        // Assume auth check done at controller
        Page<CheckInEntity> page = checkInRepository.findByGymIdOrderByCheckInTimeDesc(gymId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserCheckedInToGym(UUID userId, UUID gymId) {
        return checkInRepository.existsByUserIdAndGymId(userId, gymId);
    }
    
    private CheckInResponse mapToResponse(CheckInEntity checkIn) {
        return CheckInResponse.builder()
                .id(checkIn.getId())
                .userId(checkIn.getUserId())
                .gymId(checkIn.getGymId())
                .bookingId(checkIn.getBookingId())
                .checkInTime(checkIn.getCheckInTime())
                .status(checkIn.getStatus())
                .build();
    }
    
    private String decryptSimulate(String data) {
        // Simulate decryption - in a real scenario, this would use QrService + AES-GCM
        // We'll just assume the data passed is the raw gym ID for this mock or we can extract it if it's JSON
        return data;
    }
}
