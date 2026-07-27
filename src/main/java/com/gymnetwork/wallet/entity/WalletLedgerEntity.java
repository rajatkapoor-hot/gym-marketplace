package com.gymnetwork.wallet.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallet_ledgers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_wallet_ledger_idempotency", columnNames = {"wallet_id", "reference_id", "category"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletLedgerEntity extends BaseEntity {

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "type", nullable = false)
    private String type; // CREDIT, DEBIT

    @Column(name = "category", nullable = false)
    private String category; // RECHARGE, CHECKIN_DEDUCTION, REFUND, CANCELLATION

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
