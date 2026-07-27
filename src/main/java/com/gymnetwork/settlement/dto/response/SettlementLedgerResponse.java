package com.gymnetwork.settlement.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SettlementLedgerResponse {
    private UUID id;
    private UUID gymId;
    private UUID checkInId;
    private BigDecimal bookingAmount;
    private BigDecimal platformFee;
    private BigDecimal netAmount;
    private String status;
    private Instant createdAt;
}
