package com.gymnetwork.gym.repository;

import com.gymnetwork.gym.entity.GymEntity;
import com.gymnetwork.shared.enums.GymStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymRepository extends JpaRepository<GymEntity, UUID>, JpaSpecificationExecutor<GymEntity> {
    Optional<GymEntity> findBySlug(String slug);
    List<GymEntity> findByOwnerId(UUID ownerId);
    Page<GymEntity> findByStatus(GymStatus status, Pageable pageable);

    @Query(value = "SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) AS distance FROM gyms WHERE status = 'APPROVED' AND deleted = false ORDER BY distance ASC", nativeQuery = true)
    List<GymEntity> findNearbyGyms(@Param("lat") double lat, @Param("lng") double lng, Pageable pageable);

    Page<GymEntity> findByStatusOrderByRatingAverageDesc(GymStatus status, Pageable pageable);
}
