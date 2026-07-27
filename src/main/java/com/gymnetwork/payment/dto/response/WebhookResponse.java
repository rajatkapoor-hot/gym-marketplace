package com.gymnetwork.payment.dto.response;

import lombok.Builder;

@Builder
public record WebhookResponse(String event, String orderId, WebhookStatus status) {
    public enum WebhookStatus {
        HANDLED,
        DUPLICATE,
        IGNORED,
        UNKNOWN_ORDER
    }
}
