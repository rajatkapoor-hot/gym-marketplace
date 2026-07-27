package com.gymnetwork.gym.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "gym_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymImageEntity extends BaseEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "image_url", nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "is_cover")
    @Builder.Default
    private Boolean isCover = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;
}
