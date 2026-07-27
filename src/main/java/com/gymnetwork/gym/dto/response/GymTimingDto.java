package com.gymnetwork.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymTimingDto {
    private UUID id;
    private String dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotCapacity;
}
