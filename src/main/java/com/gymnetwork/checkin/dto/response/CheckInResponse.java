package com.gymnetwork.checkin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CheckInResponse {
    private UUID id;
    private UUID userId;
    private UUID gymId;
    private UUID bookingId;
    private LocalDateTime checkInTime;
    private String status;
}
