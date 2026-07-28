package com.gymnetwork.qr.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.qr.dto.response.QrResponse;
import com.gymnetwork.shared.dto.QrPayload;
import com.gymnetwork.shared.service.GymInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrServiceImplTest {

    private static final byte[] PNG_SIGNATURE = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    @Mock
    private GymInternalService gymInternalService;

    private QrServiceImpl qrService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        qrService = new QrServiceImpl(gymInternalService, objectMapper, null);
        ReflectionTestUtils.setField(qrService, "secretKey", "test-secret-key-1234567890");
        ReflectionTestUtils.setField(qrService, "qrTtl", Duration.ofHours(24));
    }

    @Test
    void encryptsAndDecryptsStructuredPayload() {
        UUID gymId = UUID.randomUUID();
        when(gymInternalService.existsById(gymId)).thenReturn(true);

        QrResponse response = qrService.getGymQr(gymId);
        QrPayload payload = qrService.decryptPayload(response.getEncryptedPayload());

        assertThat(payload.gymId()).isEqualTo(gymId);
        assertThat(payload.version()).isEqualTo(1);
        assertThat(payload.issuedAt()).isNotNull();
        assertThat(payload.expiresAt()).isAfter(payload.issuedAt());
    }

    @Test
    void rejectsExpiredPayload() {
        UUID gymId = UUID.randomUUID();
        when(gymInternalService.existsById(gymId)).thenReturn(true);
        ReflectionTestUtils.setField(qrService, "qrTtl", Duration.ofSeconds(-1));

        QrResponse response = qrService.getGymQr(gymId);

        assertThatThrownBy(() -> qrService.decryptPayload(response.getEncryptedPayload()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("QR code has expired");
    }

    @Test
    void rejectsTamperedPayload() {
        UUID gymId = UUID.randomUUID();
        when(gymInternalService.existsById(gymId)).thenReturn(true);
        QrResponse response = qrService.getGymQr(gymId);
        byte[] payloadBytes = Base64.getDecoder().decode(response.getEncryptedPayload());
        payloadBytes[payloadBytes.length - 1] ^= 1;
        String tamperedPayload = Base64.getEncoder().encodeToString(payloadBytes);

        assertThatThrownBy(() -> qrService.decryptPayload(tamperedPayload))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid or tampered QR code payload");
    }

    @Test
    void rejectsPayloadMissingRequiredFields() {
        QrPayload missingGymId = new QrPayload(null, Instant.now(), Instant.now().plusSeconds(60), 1);
        String encryptedPayload = ReflectionTestUtils.invokeMethod(qrService, "encryptPayload", missingGymId);

        assertThatThrownBy(() -> qrService.decryptPayload(encryptedPayload))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("QR code payload is missing required fields");
    }

    @Test
    void rejectsMalformedBase64Payload() {
        assertThatThrownBy(() -> qrService.decryptPayload("not valid base64!*"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Malformed QR code payload");
    }

    @Test
    void generateQrImageReturnsPngBytes() {
        UUID gymId = UUID.randomUUID();
        when(gymInternalService.existsById(gymId)).thenReturn(true);

        byte[] imageBytes = qrService.generateQrImage(gymId);

        assertThat(imageBytes).startsWith(PNG_SIGNATURE);
    }

    @Test
    void getGymQrBuildsDataUrlFromPngBytesAndKeepsEncryptedPayload() {
        UUID gymId = UUID.randomUUID();
        when(gymInternalService.existsById(gymId)).thenReturn(true);

        var response = qrService.getGymQr(gymId);
        byte[] dataUrlBytes = Base64.getDecoder().decode(response.getQrCodeDataUrl().substring("data:image/png;base64,".length()));

        assertThat(response.getEncryptedPayload()).isNotBlank();
        assertThat(dataUrlBytes).startsWith(PNG_SIGNATURE);
    }
}
