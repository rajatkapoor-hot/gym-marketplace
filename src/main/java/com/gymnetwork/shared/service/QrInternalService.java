package com.gymnetwork.shared.service;

import java.util.UUID;

public interface QrInternalService {
    UUID decryptGymId(String encryptedPayload);
}
