package com.gymnetwork.owner.repository;

import com.gymnetwork.owner.entity.GymOwnerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymOwnerProfileRepository extends JpaRepository<GymOwnerProfileEntity, UUID> {
    Optional<GymOwnerProfileEntity> findByUserId(UUID userId);
}
