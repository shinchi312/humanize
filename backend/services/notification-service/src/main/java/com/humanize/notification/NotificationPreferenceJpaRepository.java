package com.humanize.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceJpaRepository extends JpaRepository<NotificationPreferenceEntity, String> {
}
