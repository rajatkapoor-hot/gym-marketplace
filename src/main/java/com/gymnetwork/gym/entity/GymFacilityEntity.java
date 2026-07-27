package com.gymnetwork.gym.entity;

import com.gymnetwork.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "gym_facilities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymFacilityEntity extends BaseEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "facility_name", nullable = false)
    private String facilityName;

    @Column(name = "icon_name")
    private String iconName;

    @Column(name = "description")
    private String description;
}
