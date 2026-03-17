package com.humanize.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.NotificationLifecyclePayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AiEventListener {
    private static final Logger log = LoggerFactory.getLogger(AiEventListener.class);

    private final ObjectMapper objectMapper;
    private final DomainEventPublisher eventPublisher;
    private final SpoilerGenerationService spoilerGenerationService;

    public AiEventListener(
            ObjectMapper objectMapper,
            DomainEventPublisher eventPublisher,
            SpoilerGenerationService spoilerGenerationService
    ) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.spoilerGenerationService = spoilerGenerationService;
    }

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_LIFECYCLE, groupId = "ai-service")
    public void onNotificationLifecycle(DomainEvent event) {
        if (event.type() != EventType.NOTIFICATION_SPOILER_REQUESTED) {
            return;
        }

        NotificationLifecyclePayload request = toPayload(event, NotificationLifecyclePayload.class);
        SpoilerGenerationService.GeneratedSpoiler generatedSpoiler = spoilerGenerationService.generate(request);
        NotificationLifecyclePayload generated = new NotificationLifecyclePayload(
                request.userId(),
                request.bookId(),
                "SPOILER_GENERATED",
                request.channel(),
                generatedSpoiler.text()
        );
        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_LIFECYCLE,
                EventType.NOTIFICATION_SPOILER_GENERATED,
                request.bookId(),
                generated,
                "ai-service"
        );
        log.info("ai-service generated spoiler user={} book={} provider={} model={}",
                request.userId(), request.bookId(), generatedSpoiler.provider(), generatedSpoiler.model());
    }

    private <T> T toPayload(DomainEvent event, Class<T> payloadClass) {
        try {
            return objectMapper.treeToValue(event.payload(), payloadClass);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid payload for " + payloadClass.getSimpleName(), ex);
        }
    }
}
