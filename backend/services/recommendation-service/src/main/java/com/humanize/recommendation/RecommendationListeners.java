package com.humanize.recommendation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.BookMetadataExtractedPayload;
import com.humanize.contracts.payload.ReaderActivityPayload;
import com.humanize.contracts.payload.ReaderProgressPayload;
import com.humanize.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RecommendationListeners {
    private static final Logger log = LoggerFactory.getLogger(RecommendationListeners.class);

    private final ObjectMapper objectMapper;
    private final RecommendationScoringService scoringService;

    public RecommendationListeners(ObjectMapper objectMapper, RecommendationScoringService scoringService) {
        this.objectMapper = objectMapper;
        this.scoringService = scoringService;
    }

    @KafkaListener(topics = KafkaTopics.READER_PROGRESS, groupId = "recommendation-service")
    public void onReaderProgress(DomainEvent event) {
        ReaderProgressPayload payload = toPayload(event, ReaderProgressPayload.class);
        scoringService.applyProgress(payload);
        log.info("reco consumed progress eventId={} key={}", event.eventId(), event.partitionKey());
    }

    @KafkaListener(topics = KafkaTopics.READER_ACTIVITY, groupId = "recommendation-service")
    public void onReaderActivity(DomainEvent event) {
        ReaderActivityPayload payload = toPayload(event, ReaderActivityPayload.class);
        scoringService.applyActivity(payload);
        log.info("reco consumed activity eventId={} key={}", event.eventId(), event.partitionKey());
    }

    @KafkaListener(topics = KafkaTopics.BOOK_PROCESSING, groupId = "recommendation-service")
    public void onBookProcessing(DomainEvent event) {
        if (event.type() != EventType.BOOK_METADATA_EXTRACTED) {
            return;
        }
        BookMetadataExtractedPayload payload = toPayload(event, BookMetadataExtractedPayload.class);
        scoringService.applyMetadata(payload);
        log.info("reco consumed metadata eventId={} key={} genre={}",
                event.eventId(), event.partitionKey(), payload.genre());
    }

    private <T> T toPayload(DomainEvent event, Class<T> payloadClass) {
        try {
            return objectMapper.treeToValue(event.payload(), payloadClass);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid payload for " + payloadClass.getSimpleName(), ex);
        }
    }
}
