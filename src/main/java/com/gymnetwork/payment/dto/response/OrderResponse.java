package com.gymnetwork.payment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID paymentId;
    private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
}
