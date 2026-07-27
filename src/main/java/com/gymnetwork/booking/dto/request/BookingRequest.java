package com.gymnetwork.booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class BookingRequest {
    @NotNull
    private UUID gymId;
    
    @NotNull
    @FutureOrPresent
    private LocalDate bookingDate;
    
    @NotNull
    private LocalTime entryTime;
}
