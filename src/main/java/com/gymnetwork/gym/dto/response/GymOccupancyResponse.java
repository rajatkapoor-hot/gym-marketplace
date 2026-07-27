package com.gymnetwork.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymOccupancyResponse {
    private UUID gymId;
    private int currentOccupancy;
    private int maxCapacity;
    private double percentage;
}
