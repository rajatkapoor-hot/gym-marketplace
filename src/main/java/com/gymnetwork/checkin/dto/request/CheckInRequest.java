package com.gymnetwork.checkin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CheckInRequest {
    @NotNull
    private UUID bookingId;
    
    @NotBlank
    private String qrData;
}
