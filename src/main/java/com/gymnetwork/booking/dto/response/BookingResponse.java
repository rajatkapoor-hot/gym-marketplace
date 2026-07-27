package com.gymnetwork.booking.dto.response;

import com.gymnetwork.shared.enums.BookingStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID id;
    private UUID userId;
    private UUID gymId;
    private LocalDate bookingDate;
    private LocalTime entryTime;
    private LocalTime exitTime;
    private BookingStatus status;
    private BigDecimal amount;
}
