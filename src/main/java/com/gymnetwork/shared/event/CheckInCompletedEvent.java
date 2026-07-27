package com.gymnetwork.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInCompletedEvent {
    private UUID checkInId;
    private UUID bookingId;
    private UUID userId;
    private UUID gymId;
    private BigDecimal amount;
}
