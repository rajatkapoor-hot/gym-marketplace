package com.gymnetwork.payment.service;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.payment.dto.request.CreateOrderRequest;
import com.gymnetwork.payment.dto.request.VerifyPaymentRequest;
import com.gymnetwork.payment.dto.response.OrderResponse;
import com.gymnetwork.payment.dto.response.PaymentResponse;
import org.springframework.data.domain.Pageable;


import java.util.UUID;

public interface PaymentService {
    OrderResponse createOrder(UUID userId, CreateOrderRequest request);
    PaymentResponse verifyPayment(UUID userId, VerifyPaymentRequest request);
    void handleWebhook(String payload, String signature);
    PageResponse<PaymentResponse> getPaymentHistory(UUID userId, Pageable pageable);
}
