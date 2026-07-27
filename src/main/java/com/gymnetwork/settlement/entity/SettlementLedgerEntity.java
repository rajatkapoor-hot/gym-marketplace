package com.gymnetwork.settlement.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "settlement_ledgers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementLedgerEntity extends BaseEntity {

    @Column(name = "gym_owner_id", nullable = false)
    private UUID gymOwnerId;

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "check_in_id", nullable = false)
    private UUID checkInId;
    
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "booking_amount", nullable = false)
    private BigDecimal bookingAmount;

    @Column(name = "platform_fee", nullable = false)
    private BigDecimal platformFee;
    
    @Column(name = "net_amount", nullable = false)
    private BigDecimal netAmount;

    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PROCESSED
    
    @Column(name = "payout_reference")
    private String payoutReference;
}
