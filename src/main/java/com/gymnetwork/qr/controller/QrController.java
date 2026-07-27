package com.gymnetwork.qr.controller;

import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.qr.dto.request.RegenerateQrRequest;
import com.gymnetwork.qr.dto.response.QrResponse;
import com.gymnetwork.qr.service.QrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
@Tag(name = "QR Management", description = "Encrypted Gym QR Code Generation & Retrieval APIs")
public class QrController {

    private final QrService qrService;

    @GetMapping("/gym/{gymId}")
    @Operation(summary = "Get permanent encrypted QR code payload for gym")
    public ResponseEntity<ApiResponse<QrResponse>> getGymQr(@PathVariable UUID gymId) {
        return ResponseEntity.ok(ApiResponse.success(qrService.getGymQr(gymId)));
    }

    @PostMapping("/regenerate")
    @Operation(summary = "Regenerate gym encrypted QR code payload")
    public ResponseEntity<ApiResponse<QrResponse>> regenerateQr(@Valid @RequestBody RegenerateQrRequest request) {
        return ResponseEntity.ok(ApiResponse.success("QR Code regenerated", qrService.regenerateGymQr(request.getGymId())));
    }

    @GetMapping("/download")
    @Operation(summary = "Download QR code image file")
    public ResponseEntity<byte[]> downloadQr(@RequestParam UUID gymId) {
        byte[] imageBytes = qrService.generateQrImage(gymId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"gym_qr_" + gymId + ".png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }
}
