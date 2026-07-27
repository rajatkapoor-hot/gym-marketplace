package com.gymnetwork.gym.repository;

import com.gymnetwork.gym.entity.GymBadgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GymBadgeRepository extends JpaRepository<GymBadgeEntity, UUID> {
    List<GymBadgeEntity> findByGymId(UUID gymId);
}
