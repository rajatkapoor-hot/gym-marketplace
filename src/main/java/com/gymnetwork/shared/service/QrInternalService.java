package com.gymnetwork.shared.service;

import com.gymnetwork.shared.dto.QrPayload;

public interface QrInternalService {
    /**
     * Decrypts and validates a versioned JSON QR payload. Legacy raw-UUID QR codes are not accepted and must be regenerated.
     */
    QrPayload decryptPayload(String encryptedPayload);
}
