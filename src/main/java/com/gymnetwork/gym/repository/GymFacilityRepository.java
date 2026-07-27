package com.gymnetwork.gym.repository;

import com.gymnetwork.gym.entity.GymFacilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GymFacilityRepository extends JpaRepository<GymFacilityEntity, UUID> {
    List<GymFacilityEntity> findByGymId(UUID gymId);
    void deleteByGymId(UUID gymId);
}
