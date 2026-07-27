package com.gymnetwork.qr.service.impl;

import com.gymnetwork.shared.service.GymInternalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrServiceImplTest {

    private static final byte[] PNG_SIGNATURE = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    @Mock
    private GymInternalService gymInternalService;

    private QrServiceImpl qrService;

    @BeforeEach
    void setUp() {
        qrService = new QrServiceImpl(gymInternalService);
        ReflectionTestUtils.setField(qrService, "secretKey", "3c9a1e8f2b5d7a4c6e0f2a4b6c8d0e2f");
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
