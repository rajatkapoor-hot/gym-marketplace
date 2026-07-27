package com.gymnetwork.settlement.repository;

import com.gymnetwork.settlement.entity.SettlementLedgerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SettlementLedgerRepository extends JpaRepository<SettlementLedgerEntity, UUID> {
    Page<SettlementLedgerEntity> findByGymOwnerIdOrderByCreatedAtDesc(UUID gymOwnerId, Pageable pageable);
}
