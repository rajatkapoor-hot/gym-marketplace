package com.gymnetwork.payment.service.impl;

import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.payment.dto.response.WebhookResponse;
import com.gymnetwork.payment.entity.PaymentEntity;
import com.gymnetwork.payment.repository.PaymentRepository;
import com.gymnetwork.shared.enums.PaymentStatus;
import com.gymnetwork.shared.service.WalletInternalService;
import com.gymnetwork.wallet.dto.request.RechargeWalletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceImplTest {

    private static final String WEBHOOK_SECRET = "test-webhook-secret";

    private PaymentRepository paymentRepository;
    private WalletInternalService walletInternalService;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        walletInternalService = mock(WalletInternalService.class);
        paymentService = new PaymentServiceImpl(paymentRepository, walletInternalService);
        ReflectionTestUtils.setField(paymentService, "razorpayWebhookSecret", WEBHOOK_SECRET);
    }

    @Test
    void handleWebhookThrowsBadRequestForInvalidSignature() {
        String payload = paymentCapturedPayload("order_invalid", "pay_invalid");

        assertThatThrownBy(() -> paymentService.handleWebhook(payload, "invalid-signature"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid Razorpay webhook signature");

        verifyNoInteractions(paymentRepository, walletInternalService);
    }

    @Test
    void handleWebhookThrowsBadRequestForMalformedPayloadAfterValidSignature() {
        String payload = "{not-json";

        assertThatThrownBy(() -> paymentService.handleWebhook(payload, signatureFor(payload)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Malformed Razorpay webhook payload");

        verifyNoInteractions(paymentRepository, walletInternalService);
    }

    @Test
    void handleWebhookReturnsUnknownOrderForValidPaymentCapturedEvent() {
        String payload = paymentCapturedPayload("order_unknown", "pay_unknown");
        when(paymentRepository.findByRazorpayOrderId("order_unknown")).thenReturn(Optional.empty());

        WebhookResponse response = paymentService.handleWebhook(payload, signatureFor(payload));

        assertThat(response.status()).isEqualTo(WebhookResponse.WebhookStatus.UNKNOWN_ORDER);
        assertThat(response.event()).isEqualTo("payment.captured");
        assertThat(response.orderId()).isEqualTo("order_unknown");
        verify(paymentRepository).findByRazorpayOrderId("order_unknown");
        verifyNoInteractions(walletInternalService);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleWebhookReturnsDuplicateForAlreadySuccessfulPayment() {
        String payload = paymentCapturedPayload("order_duplicate", "pay_duplicate");
        PaymentEntity payment = payment("order_duplicate", PaymentStatus.SUCCESS);
        when(paymentRepository.findByRazorpayOrderId("order_duplicate")).thenReturn(Optional.of(payment));

        WebhookResponse response = paymentService.handleWebhook(payload, signatureFor(payload));

        assertThat(response.status()).isEqualTo(WebhookResponse.WebhookStatus.DUPLICATE);
        assertThat(response.orderId()).isEqualTo("order_duplicate");
        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(walletInternalService);
    }

    @Test
    void handleWebhookProcessesValidPaymentCapturedEventIdempotently() {
        String payload = paymentCapturedPayload("order_valid", "pay_valid");
        PaymentEntity payment = payment("order_valid", PaymentStatus.CREATED);
        when(paymentRepository.findByRazorpayOrderId("order_valid")).thenReturn(Optional.of(payment));

        WebhookResponse response = paymentService.handleWebhook(payload, signatureFor(payload));

        assertThat(response.status()).isEqualTo(WebhookResponse.WebhookStatus.HANDLED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getRazorpayPaymentId()).isEqualTo("pay_valid");
        verify(paymentRepository).save(payment);

        ArgumentCaptor<RechargeWalletRequest> rechargeCaptor = ArgumentCaptor.forClass(RechargeWalletRequest.class);
        verify(walletInternalService).rechargeWallet(eq(payment.getUserId()), rechargeCaptor.capture());
        assertThat(rechargeCaptor.getValue().getAmount()).isEqualByComparingTo("499.00");
        assertThat(rechargeCaptor.getValue().getPaymentReferenceId()).isEqualTo("pay_valid");
    }

    private PaymentEntity payment(String orderId, PaymentStatus status) {
        return PaymentEntity.builder()
                .userId(UUID.randomUUID())
                .razorpayOrderId(orderId)
                .amount(new BigDecimal("499.00"))
                .currency("INR")
                .status(status)
                .build();
    }

    private String paymentCapturedPayload(String orderId, String paymentId) {
        return """
                {"event":"payment.captured","payload":{"payment":{"entity":{"id":"%s","order_id":"%s"}}}}
                """.formatted(paymentId, orderId).trim();
    }

    private String signatureFor(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
