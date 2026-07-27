package com.gymnetwork.qr.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrResponse {
    private UUID gymId;
    private String encryptedPayload;
    private String qrCodeDataUrl; // Base64 encoded or payload string
}
