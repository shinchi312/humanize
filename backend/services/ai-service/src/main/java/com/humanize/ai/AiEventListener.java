package com.humanize.ai;

import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import com.humanize.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AiEventListener {
    private static final Logger log = LoggerFactory.getLogger(AiEventListener.class);

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_LIFECYCLE, groupId = "ai-service")
    public void onNotificationLifecycle(DomainEvent event) {
        if (event.type() == EventType.NOTIFICATION_SPOILER_REQUESTED) {
            log.info("ai-service received spoiler request for key={} eventId={}", event.partitionKey(), event.eventId());
        }
    }
}
