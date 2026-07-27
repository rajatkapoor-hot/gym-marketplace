package com.gymnetwork.shared.service;

import com.gymnetwork.wallet.dto.request.RechargeWalletRequest;
import com.gymnetwork.wallet.dto.response.WalletResponse;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletInternalService {
    void createWalletForUser(UUID userId);
    WalletResponse rechargeWallet(UUID userId, RechargeWalletRequest request);
    void deductWallet(UUID userId, BigDecimal amount, String referenceId, String description);
}
