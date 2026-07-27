package com.gymnetwork.shared.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface GymInternalService {
    boolean existsById(UUID gymId);
    BigDecimal getGymCommissionRate(UUID gymId);
    void updateGymRating(UUID gymId, int newRating);
    UUID getGymOwnerId(UUID gymId);
}
