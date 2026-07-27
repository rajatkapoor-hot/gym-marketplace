package com.gymnetwork.wallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeWalletRequest {
    @NotNull @DecimalMin("1.00")
    private BigDecimal amount;

    private String paymentReferenceId;
}
