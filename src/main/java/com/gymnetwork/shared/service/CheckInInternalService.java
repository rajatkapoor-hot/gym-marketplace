package com.gymnetwork.shared.service;

import java.util.UUID;

public interface CheckInInternalService {
    boolean hasUserCheckedInToGym(UUID userId, UUID gymId);
}
