package com.gymnetwork.qr.service;

import com.gymnetwork.qr.dto.response.QrResponse;

import java.util.UUID;

public interface QrService {
    QrResponse getGymQr(UUID gymId);
    QrResponse regenerateGymQr(UUID gymId);
    byte[] generateQrImage(UUID gymId);
}
