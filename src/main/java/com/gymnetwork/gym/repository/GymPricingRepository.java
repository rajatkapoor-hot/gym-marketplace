package com.gymnetwork.gym.repository;

import com.gymnetwork.gym.entity.GymPricingEntity;
import com.gymnetwork.shared.enums.PassType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymPricingRepository extends JpaRepository<GymPricingEntity, UUID> {
    List<GymPricingEntity> findByGymIdAndIsActiveTrue(UUID gymId);
    Optional<GymPricingEntity> findByGymIdAndPassTypeAndIsActiveTrue(UUID gymId, PassType passType);
}
