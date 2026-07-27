package com.gymnetwork.wallet.repository;

import com.gymnetwork.wallet.entity.WalletLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletLedgerRepository extends JpaRepository<WalletLedgerEntity, UUID> {
    Page<WalletLedgerEntity> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable);
    List<WalletLedgerEntity> findByWalletIdOrderByCreatedAtDesc(UUID walletId);
    boolean existsByWalletIdAndReferenceIdAndCategory(UUID walletId, String referenceId, String category);
}
