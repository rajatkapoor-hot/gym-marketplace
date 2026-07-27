package com.gymnetwork.gym.repository;

import com.gymnetwork.gym.entity.GymTimingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymTimingRepository extends JpaRepository<GymTimingEntity, UUID> {
    List<GymTimingEntity> findByGymId(UUID gymId);
    Optional<GymTimingEntity> findByGymIdAndDayOfWeek(UUID gymId, String dayOfWeek);
    void deleteByGymId(UUID gymId);
}
