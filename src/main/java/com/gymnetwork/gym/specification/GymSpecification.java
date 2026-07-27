package com.gymnetwork.gym.specification;

import com.gymnetwork.gym.entity.GymEntity;
import com.gymnetwork.shared.enums.GymStatus;
import org.springframework.data.jpa.domain.Specification;

public class GymSpecification {

    public static Specification<GymEntity> isApproved() {
        return (root, query, cb) -> cb.equal(root.get("status"), GymStatus.APPROVED);
    }

    public static Specification<GymEntity> hasCity(String city) {
        return (root, query, cb) -> (city == null || city.isBlank()) ? null : cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<GymEntity> searchByNameOrAddress(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("addressLine1")), pattern),
                    cb.like(cb.lower(root.get("city")), pattern)
            );
        };
    }
}
