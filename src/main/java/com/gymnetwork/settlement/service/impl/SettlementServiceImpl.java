package com.gymnetwork.settlement.service.impl;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.settlement.dto.response.SettlementLedgerResponse;
import com.gymnetwork.settlement.entity.SettlementLedgerEntity;
import com.gymnetwork.settlement.repository.SettlementLedgerRepository;
import com.gymnetwork.settlement.service.SettlementService;
import com.gymnetwork.shared.event.CheckInCompletedEvent;
import com.gymnetwork.shared.service.GymInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementLedgerRepository settlementLedgerRepository;
    private final GymInternalService gymInternalService;

    @Async
    @EventListener
    @Transactional
    public void handleCheckInCompletedEvent(CheckInCompletedEvent event) {
        log.info("Processing settlement for checkInId: {}", event.getCheckInId());
        
        UUID ownerId = gymInternalService.getGymOwnerId(event.getGymId());
        
        // Simple logic: Assuming 10% platform fee if not defined differently
        // Here we could use gymInternalService to get specific commission rate
        BigDecimal platformFeePercentage = new BigDecimal("0.10"); 
        
        BigDecimal platformFee = event.getAmount().multiply(platformFeePercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = event.getAmount().subtract(platformFee);
        
        SettlementLedgerEntity ledger = SettlementLedgerEntity.builder()
                .gymOwnerId(ownerId)
                .gymId(event.getGymId())
                .checkInId(event.getCheckInId())
                .bookingId(event.getBookingId())
                .bookingAmount(event.getAmount())
                .platformFee(platformFee)
                .netAmount(netAmount)
                .status("PENDING")
                .build();
                
        settlementLedgerRepository.save(ledger);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SettlementLedgerResponse> getOwnerSettlements(UUID ownerId, Pageable pageable) {
        Page<SettlementLedgerEntity> page = settlementLedgerRepository.findByGymOwnerIdOrderByCreatedAtDesc(ownerId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }
    
    private SettlementLedgerResponse mapToResponse(SettlementLedgerEntity ledger) {
        return SettlementLedgerResponse.builder()
                .id(ledger.getId())
                .gymId(ledger.getGymId())
                .checkInId(ledger.getCheckInId())
                .bookingAmount(ledger.getBookingAmount())
                .platformFee(ledger.getPlatformFee())
                .netAmount(ledger.getNetAmount())
                .status(ledger.getStatus())
                .createdAt(ledger.getCreatedAt())
                .build();
    }
}
