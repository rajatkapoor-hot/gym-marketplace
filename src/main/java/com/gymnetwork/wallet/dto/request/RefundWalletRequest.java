package com.gymnetwork.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RefundWalletRequest {
    @NotNull @DecimalMin("1.00")
    private BigDecimal amount;
    
    @NotBlank
    private String reason;
    
    @NotNull
    private UUID bookingId;
}
