package com.gymnetwork.shared.dto;

import java.time.Instant;
import java.util.UUID;

public record QrPayload(
        UUID gymId,
        Instant issuedAt,
        Instant expiresAt,
        int version
) {
}
