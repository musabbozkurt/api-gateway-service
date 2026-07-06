package com.mb.notificationservice.data.specification;

import com.mb.notificationservice.api.request.NotificationFilterRequest;
import com.mb.notificationservice.data.entity.Notification;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationSpecification {

    public static Specification<Notification> init(Long userId, NotificationFilterRequest filter) {
        return (root, _, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("userId"), userId));

            if (Objects.nonNull(filter.getChannel())) {
                predicates.add(criteriaBuilder.equal(root.get("channel"), filter.getChannel()));
            }

            if (Objects.nonNull(filter.getIsRead())) {
                predicates.add(criteriaBuilder.equal(root.get("isRead"), filter.getIsRead()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
