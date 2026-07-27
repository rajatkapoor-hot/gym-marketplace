package com.gymnetwork.shared.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private UUID bookingId;
    private String bookingCode;
    private UUID userId;
    private UUID gymId;
    private String passType;
    private BigDecimal price;
    private LocalDate bookingDate;
}
