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
public class PaymentSuccessEvent {
    private UUID paymentId;
    private UUID userId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private BigDecimal amount;
}
