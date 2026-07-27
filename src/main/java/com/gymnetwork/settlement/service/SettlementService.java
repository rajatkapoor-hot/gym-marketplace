package com.gymnetwork.settlement.service;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.settlement.dto.response.SettlementLedgerResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SettlementService {
    PageResponse<SettlementLedgerResponse> getOwnerSettlements(UUID ownerId, Pageable pageable);
}
