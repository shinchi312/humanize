package com.humanize.notification;

import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.NotificationLifecyclePayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final DomainEventPublisher eventPublisher;

    public NotificationController(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/email/sent")
    public Map<String, Object> markEmailSent(@Valid @RequestBody NotificationLifecyclePayload payload) {
        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_LIFECYCLE,
                EventType.NOTIFICATION_EMAIL_SENT,
                payload.bookId(),
                payload,
                "notification-service"
        );

        return Map.of("status", "recorded", "bookId", payload.bookId(), "channel", payload.channel());
    }
}
