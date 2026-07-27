package com.gymnetwork.qr.service.impl;

import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.gymnetwork.qr.dto.response.QrResponse;
import com.gymnetwork.qr.service.QrService;
import com.gymnetwork.shared.service.GymInternalService;
import com.gymnetwork.shared.service.QrInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrServiceImpl implements QrService, QrInternalService {

    private final GymInternalService gymInternalService;

    @Value("${app.qr.encryption-key:3c9a1e8f2b5d7a4c6e0f2a4b6c8d0e2f}")
    private String secretKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int QR_CODE_SIZE = 300;
    private static final String PNG_FORMAT = "PNG";

    @Override
    public QrResponse getGymQr(UUID gymId) {
        if (!gymInternalService.existsById(gymId)) {
            throw new ResourceNotFoundException("Gym not found with ID: " + gymId);
        }
        String encryptedPayload = encryptGymId(gymId);
        return QrResponse.builder()
                .gymId(gymId)
                .encryptedPayload(encryptedPayload)
                .qrCodeDataUrl("data:image/png;base64," + Base64.getEncoder().encodeToString(generateQrPng(encryptedPayload)))
                .build();
    }

    @Override
    public QrResponse regenerateGymQr(UUID gymId) {
        return getGymQr(gymId);
    }

    @Override
    public byte[] generateQrImage(UUID gymId) {
        if (!gymInternalService.existsById(gymId)) {
            throw new ResourceNotFoundException("Gym not found with ID: " + gymId);
        }
        return generateQrPng(encryptGymId(gymId));
    }

    @Override
    public UUID decryptGymId(String encryptedPayload) {
        try {
            byte[] cipherTextWithIv = Base64.getDecoder().decode(encryptedPayload);
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(cipherTextWithIv, 0, iv, 0, IV_LENGTH);

            int cipherTextSize = cipherTextWithIv.length - IV_LENGTH;
            byte[] cipherText = new byte[cipherTextSize];
            System.arraycopy(cipherTextWithIv, IV_LENGTH, cipherText, 0, cipherTextSize);

            SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] plainText = cipher.doFinal(cipherText);
            String decryptedStr = new String(plainText, StandardCharsets.UTF_8);
            return UUID.fromString(decryptedStr);
        } catch (Exception e) {
            log.error("Failed to decrypt QR payload", e);
            throw new BadRequestException("Invalid or tampered QR code payload");
        }
    }

    private byte[] generateQrPng(String encryptedPayload) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(encryptedPayload, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, PNG_FORMAT, outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            log.error("Error generating QR PNG image", e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }

    private String encryptGymId(UUID gymId) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(getKeyBytes(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] cipherText = cipher.doFinal(gymId.toString().getBytes(StandardCharsets.UTF_8));
            byte[] finalPayload = new byte[IV_LENGTH + cipherText.length];

            System.arraycopy(iv, 0, finalPayload, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, finalPayload, IV_LENGTH, cipherText.length);

            return Base64.getEncoder().encodeToString(finalPayload);
        } catch (Exception e) {
            log.error("Error encrypting Gym UUID for QR code", e);
            throw new RuntimeException("Failed to generate encrypted QR code", e);
        }
    }

    private byte[] getKeyBytes() {
        byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[16];
        System.arraycopy(key, 0, result, 0, Math.min(key.length, 16));
        return result;
    }
}
