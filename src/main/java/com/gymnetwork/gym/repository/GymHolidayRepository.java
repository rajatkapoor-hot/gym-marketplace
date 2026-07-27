package com.gymnetwork.gym.repository;

import com.gymnetwork.gym.entity.GymHolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface GymHolidayRepository extends JpaRepository<GymHolidayEntity, UUID> {
    List<GymHolidayEntity> findByGymId(UUID gymId);
    boolean existsByGymIdAndHolidayDate(UUID gymId, LocalDate holidayDate);
}
