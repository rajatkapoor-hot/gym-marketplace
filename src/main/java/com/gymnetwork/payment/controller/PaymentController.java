package com.gymnetwork.payment.controller;

import com.gymnetwork.auth.security.UserPrincipal;
import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.payment.dto.request.CreateOrderRequest;
import com.gymnetwork.payment.dto.request.VerifyPaymentRequest;
import com.gymnetwork.payment.dto.response.OrderResponse;
import com.gymnetwork.payment.dto.response.PaymentResponse;
import com.gymnetwork.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Razorpay Integration APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create a Razorpay order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", paymentService.createOrder(principal.getId(), request)));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify Razorpay payment signature")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", paymentService.verifyPayment(principal.getId(), request)));
    }

    @PostMapping("/webhook")
    @Operation(
            summary = "Razorpay Webhook endpoint",
            description = "Provider callback endpoint. Successful responses intentionally return an empty body instead of ApiResponse so Razorpay receives only an HTTP status acknowledgment; invalid signatures and malformed payloads return the standard error envelope.")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    @Operation(summary = "Get user payment history")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getPaymentHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentHistory(principal.getId(), PageRequest.of(page, size))));
    }
}
