package com.humanize.notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryLogJpaRepository extends JpaRepository<NotificationDeliveryLogEntity, Long> {
    Optional<NotificationDeliveryLogEntity> findTopByUserIdAndBookIdAndStatusOrderByCreatedAtDesc(
            String userId,
            String bookId,
            String status
    );

    long countByUserIdAndBookIdAndStatusAndCreatedAtAfter(
            String userId,
            String bookId,
            String status,
            Instant createdAfter
    );

    List<NotificationDeliveryLogEntity> findTop50ByUserIdOrderByCreatedAtDesc(String userId);

    List<NotificationDeliveryLogEntity> findTop20ByUserIdAndBookIdOrderByCreatedAtDesc(String userId, String bookId);
}
