package com.gymnetwork.payment.service.impl;

import com.gymnetwork.BaseIntegrationTest;
import com.gymnetwork.payment.dto.request.VerifyPaymentRequest;
import com.gymnetwork.payment.entity.PaymentEntity;
import com.gymnetwork.payment.repository.PaymentRepository;
import com.gymnetwork.payment.service.PaymentService;
import com.gymnetwork.shared.enums.PaymentStatus;
import com.gymnetwork.wallet.entity.WalletEntity;
import com.gymnetwork.wallet.repository.WalletLedgerRepository;
import com.gymnetwork.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentServiceImplIdempotencyTest extends BaseIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletLedgerRepository walletLedgerRepository;

    @BeforeEach
    void setUp() {
        walletLedgerRepository.deleteAll();
        paymentRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void verifyThenWebhookCreditsWalletOnlyOnce() throws Exception {
        UUID userId = UUID.randomUUID();
        createWallet(userId);
        createPayment(userId, "order_verify_first", "100.00");

        paymentService.verifyPayment(userId, verifyRequest("order_verify_first", "pay_same_reference"));
        String payload = webhookPayload("order_verify_first", "pay_same_reference");
        paymentService.handleWebhook(payload, webhookSignature(payload));

        assertWalletWasCreditedOnce(userId, "pay_same_reference", "100.00");
    }

    @Test
    void webhookThenVerifyCreditsWalletOnlyOnce() throws Exception {
        UUID userId = UUID.randomUUID();
        createWallet(userId);
        createPayment(userId, "order_webhook_first", "125.00");

        String payload = webhookPayload("order_webhook_first", "pay_same_reference");
        paymentService.handleWebhook(payload, webhookSignature(payload));
        paymentService.verifyPayment(userId, verifyRequest("order_webhook_first", "pay_same_reference"));

        assertWalletWasCreditedOnce(userId, "pay_same_reference", "125.00");
    }

    @Test
    void concurrentDuplicateWebhookDeliveryCreditsWalletOnlyOnce() throws Exception {
        UUID userId = UUID.randomUUID();
        createWallet(userId);
        createPayment(userId, "order_concurrent_webhook", "150.00");
        String payload = webhookPayload("order_concurrent_webhook", "pay_same_reference");
        int deliveries = 3;
        CountDownLatch ready = new CountDownLatch(deliveries);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(deliveries);

        String signature = webhookSignature(payload);
        for (int i = 0; i < deliveries; i++) {
            executor.submit(() -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                paymentService.handleWebhook(payload, signature);
                return null;
            });
        }

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertWalletWasCreditedOnce(userId, "pay_same_reference", "150.00");
    }

    private void createWallet(UUID userId) {
        walletRepository.save(WalletEntity.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .status("ACTIVE")
                .build());
    }

    private void createPayment(UUID userId, String orderId, String amount) {
        paymentRepository.save(PaymentEntity.builder()
                .userId(userId)
                .razorpayOrderId(orderId)
                .amount(new BigDecimal(amount))
                .currency("INR")
                .status(PaymentStatus.CREATED)
                .build());
    }

    private VerifyPaymentRequest verifyRequest(String orderId, String paymentId) {
        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId(orderId);
        request.setRazorpayPaymentId(paymentId);
        request.setRazorpaySignature(paymentSignature(orderId, paymentId));
        return request;
    }

    private String webhookPayload(String orderId, String paymentId) {
        return """
                {"event":"payment.captured","payload":{"payment":{"entity":{"order_id":"%s","id":"%s"}}}}
                """.formatted(orderId, paymentId);
    }

    private String paymentSignature(String orderId, String paymentId) {
        return hmacSha256(orderId + "|" + paymentId, "mock-key-secret");
    }

    private String webhookSignature(String payload) {
        return hmacSha256(payload, "mock-webhook-secret");
    }

    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create test signature", e);
        }
    }

    private void assertWalletWasCreditedOnce(UUID userId, String referenceId, String expectedBalance) {
        WalletEntity wallet = walletRepository.findByUserId(userId).orElseThrow();
        assertThat(wallet.getBalance()).isEqualByComparingTo(expectedBalance);
        assertThat(walletLedgerRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())).hasSize(1);
        assertThat(walletLedgerRepository.existsByWalletIdAndReferenceIdAndCategory(wallet.getId(), referenceId, "RECHARGE")).isTrue();
    }
}
