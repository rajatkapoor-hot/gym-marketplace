package com.gymnetwork.user.repository;

import com.gymnetwork.user.entity.UserFavouriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFavouriteRepository extends JpaRepository<UserFavouriteEntity, UUID> {
    List<UserFavouriteEntity> findByUserId(UUID userId);
    Optional<UserFavouriteEntity> findByUserIdAndGymId(UUID userId, UUID gymId);
    boolean existsByUserIdAndGymId(UUID userId, UUID gymId);
    void deleteByUserIdAndGymId(UUID userId, UUID gymId);
}
