package com.gymnetwork.gym.repository;

import com.gymnetwork.gym.entity.GymImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GymImageRepository extends JpaRepository<GymImageEntity, UUID> {
    List<GymImageEntity> findByGymIdOrderByDisplayOrderAsc(UUID gymId);
    void deleteByGymId(UUID gymId);
}
