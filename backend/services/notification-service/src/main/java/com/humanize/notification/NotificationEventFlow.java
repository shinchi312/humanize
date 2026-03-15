package com.humanize.notification;

import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.NotificationLifecyclePayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventFlow {

    private final DomainEventPublisher eventPublisher;

    public NotificationEventFlow(DomainEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = KafkaTopics.READER_ACTIVITY, groupId = "notification-service")
    public void onReaderActivity(DomainEvent event) {
        NotificationLifecyclePayload payload = new NotificationLifecyclePayload(
                "unknown-user",
                event.partitionKey(),
                "SPOILER_REQUESTED",
                "EMAIL",
                "Potential cliffhanger generated"
        );

        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_LIFECYCLE,
                EventType.NOTIFICATION_SPOILER_REQUESTED,
                event.partitionKey(),
                payload,
                "notification-service"
        );
    }
}
