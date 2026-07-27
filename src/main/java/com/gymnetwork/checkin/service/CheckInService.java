package com.gymnetwork.checkin.service;

import com.gymnetwork.checkin.dto.request.CheckInRequest;
import com.gymnetwork.checkin.dto.response.CheckInResponse;
import com.gymnetwork.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CheckInService {
    CheckInResponse processCheckIn(UUID userId, CheckInRequest request);
    PageResponse<CheckInResponse> getUserCheckIns(UUID userId, Pageable pageable);
    PageResponse<CheckInResponse> getGymCheckIns(UUID gymOwnerId, UUID gymId, Pageable pageable);
}
