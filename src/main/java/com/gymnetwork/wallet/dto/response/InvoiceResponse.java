package com.gymnetwork.wallet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private UUID transactionId;
    private UUID walletId;
    private String invoiceNumber;
    private Instant date;
    private String transactionType;
    private String category;
    private BigDecimal amount;
    private String description;
    private UserDetails userDetails;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDetails {
        private String name;
        private String email;
    }
}
