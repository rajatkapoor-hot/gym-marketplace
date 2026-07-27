package com.gymnetwork.analytics.repository;

import com.gymnetwork.analytics.entity.GymAnalyticsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymAnalyticsRepository extends JpaRepository<GymAnalyticsEntity, UUID> {
    Optional<GymAnalyticsEntity> findByGymId(UUID gymId);
}
