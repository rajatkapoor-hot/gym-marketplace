package com.gymnetwork.user.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_favourites", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_gym_favourite", columnNames = {"user_id", "gym_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFavouriteEntity extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;
}
