package com.gymnetwork.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRechargedEvent {
    private UUID walletId;
    private UUID userId;
    private BigDecimal amount;
    private BigDecimal newBalance;
}
