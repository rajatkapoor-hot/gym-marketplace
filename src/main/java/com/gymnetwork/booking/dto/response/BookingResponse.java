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
    /**
     * UI-facing lifecycle for day-pass bookings:
     * CONFIRMED - booking is ready for QR check-in and no wallet deduction has happened yet.
     * COMPLETED - QR check-in succeeded and the wallet deduction has been applied.
     * CANCELLED - booking was cancelled before check-in.
     * PENDING is retained for legacy records only; new day-pass bookings are created as CONFIRMED.
     */
    private BookingStatus status;
    private String statusDescription;
    private BigDecimal amount;
}
