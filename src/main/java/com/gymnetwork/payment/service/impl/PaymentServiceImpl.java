package com.gymnetwork.payment.service.impl;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.payment.dto.request.CreateOrderRequest;
import com.gymnetwork.payment.dto.request.VerifyPaymentRequest;
import com.gymnetwork.payment.dto.response.OrderResponse;
import com.gymnetwork.payment.dto.response.PaymentResponse;
import com.gymnetwork.payment.dto.response.WebhookResponse;
import com.gymnetwork.payment.entity.PaymentEntity;
import com.gymnetwork.payment.repository.PaymentRepository;
import com.gymnetwork.payment.service.PaymentService;
import com.gymnetwork.shared.enums.PaymentStatus;
import com.gymnetwork.shared.service.WalletInternalService;
import com.gymnetwork.wallet.dto.request.RechargeWalletRequest;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final WalletInternalService walletInternalService;
    
    @Value("${app.razorpay.key-id:mock-key-id}")
    private String razorpayKeyId;
    
    @Value("${app.razorpay.key-secret:mock-key-secret}")
    private String razorpayKeySecret;
    
    @Value("${app.razorpay.webhook-secret:mock-webhook-secret}")
    private String razorpayWebhookSecret;

    @Override
    @Transactional
    public OrderResponse createOrder(UUID userId, CreateOrderRequest request) {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise
            orderRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", "txn_" + UUID.randomUUID().toString().substring(0, 8));
            
            Order order = razorpay.orders.create(orderRequest);
            
            PaymentEntity payment = PaymentEntity.builder()
                    .userId(userId)
                    .razorpayOrderId(order.get("id"))
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(PaymentStatus.CREATED)
                    .build();
                    
            paymentRepository.save(payment);
            
            return OrderResponse.builder()
                    .paymentId(payment.getId())
                    .razorpayOrderId(payment.getRazorpayOrderId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .build();
                    
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order", e);
            throw new RuntimeException("Failed to initiate payment. Please try again.");
        }
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(UUID userId, VerifyPaymentRequest request) {
        PaymentEntity payment = paymentRepository.findByRazorpayOrderIdForUpdate(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));
                
        if (!payment.getUserId().equals(userId)) {
            throw new BadRequestException("Payment does not belong to this user");
        }
        
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            completePaymentAndRechargeWallet(payment, request.getRazorpayPaymentId(), request.getRazorpaySignature());
            return mapToResponse(payment);
        }
        
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());
            
            boolean isValidSignature = Utils.verifyPaymentSignature(options, razorpayKeySecret);
            
            if (isValidSignature) {
                completePaymentAndRechargeWallet(payment, request.getRazorpayPaymentId(), request.getRazorpaySignature());
                return mapToResponse(payment);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new BadRequestException("Invalid payment signature");
            }
        } catch (RazorpayException e) {
            log.error("Failed to verify Razorpay signature", e);
            throw new BadRequestException("Payment verification failed");
        }
    }

    @Override
    @Transactional
    public WebhookResponse handleWebhook(String payload, String signature) {
        boolean isValid;
        try {
            isValid = Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
        } catch (RazorpayException e) {
            log.warn("Invalid Razorpay webhook signature", e);
            throw new BadRequestException("Invalid Razorpay webhook signature");
        }

        if (!isValid) {
            log.warn("Invalid Razorpay webhook signature");
            throw new BadRequestException("Invalid Razorpay webhook signature");
        }

        try {
            JSONObject jsonPayload = new JSONObject(payload);
            String event = jsonPayload.getString("event");

            if (!"payment.captured".equals(event)) {
                return WebhookResponse.builder()
                        .event(event)
                        .status(WebhookResponse.WebhookStatus.IGNORED)
                        .build();
            
            if ("payment.captured".equals(event)) {
                JSONObject paymentPayload = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
                String orderId = paymentPayload.getString("order_id");
                
                paymentRepository.findByRazorpayOrderIdForUpdate(orderId).ifPresent(payment ->
                        completePaymentAndRechargeWallet(payment, paymentPayload.getString("id"), null));
            }

            JSONObject paymentPayload = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String orderId = paymentPayload.getString("order_id");
            PaymentEntity payment = paymentRepository.findByRazorpayOrderId(orderId)
                    .orElse(null);

            if (payment == null) {
                log.warn("Received payment.captured webhook for unknown Razorpay order {}", orderId);
                return WebhookResponse.builder()
                        .event(event)
                        .orderId(orderId)
                        .status(WebhookResponse.WebhookStatus.UNKNOWN_ORDER)
                        .build();
            }

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                return WebhookResponse.builder()
                        .event(event)
                        .orderId(orderId)
                        .status(WebhookResponse.WebhookStatus.DUPLICATE)
                        .build();
            }

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setRazorpayPaymentId(paymentPayload.getString("id"));
            paymentRepository.save(payment);

            // Top-up wallet in case webhook arrives before client verify call. Duplicate SUCCESS events are ignored above.
            RechargeWalletRequest rechargeRequest = new RechargeWalletRequest();
            rechargeRequest.setAmount(payment.getAmount());
            rechargeRequest.setPaymentReferenceId(payment.getRazorpayPaymentId());
            walletInternalService.rechargeWallet(payment.getUserId(), rechargeRequest);

            return WebhookResponse.builder()
                    .event(event)
                    .orderId(orderId)
                    .status(WebhookResponse.WebhookStatus.HANDLED)
                    .build();
        } catch (JSONException e) {
            log.warn("Malformed Razorpay webhook payload", e);
            throw new BadRequestException("Malformed Razorpay webhook payload");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> getPaymentHistory(UUID userId, Pageable pageable) {
        Page<PaymentEntity> page = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }
    
    private void completePaymentAndRechargeWallet(PaymentEntity payment, String razorpayPaymentId, String razorpaySignature) {
        if (payment.getStatus() == PaymentStatus.CREATED) {
            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setRazorpaySignature(razorpaySignature);
            payment.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
        }

        RechargeWalletRequest rechargeRequest = new RechargeWalletRequest();
        rechargeRequest.setAmount(payment.getAmount());
        rechargeRequest.setPaymentReferenceId(payment.getRazorpayPaymentId() != null
                ? payment.getRazorpayPaymentId()
                : razorpayPaymentId);
        walletInternalService.rechargeWallet(payment.getUserId(), rechargeRequest);
    }

    private PaymentResponse mapToResponse(PaymentEntity payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .build();
    }
}
