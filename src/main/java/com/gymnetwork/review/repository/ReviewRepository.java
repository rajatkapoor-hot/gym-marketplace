package com.gymnetwork.review.repository;

import com.gymnetwork.review.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    Page<ReviewEntity> findByGymIdOrderByCreatedAtDesc(UUID gymId, Pageable pageable);
    
    boolean existsByUserIdAndGymId(UUID userId, UUID gymId);
    
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.gymId = :gymId AND r.deleted = false")
    Double getAverageRatingByGymId(UUID gymId);
}
