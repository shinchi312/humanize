package com.humanize.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.ReaderActivityPayload;
import com.humanize.contracts.payload.ReaderProgressPayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import java.util.Map;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReaderProgressListener {

    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final ReaderActivityRepository readerActivityRepository;

    public ReaderProgressListener(
            DomainEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            ReaderActivityRepository readerActivityRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.readerActivityRepository = readerActivityRepository;
    }

    @KafkaListener(topics = KafkaTopics.READER_PROGRESS, groupId = "activity-service")
    public void onReaderProgress(DomainEvent event) {
        ReaderProgressPayload progressPayload = toProgressPayload(event);
        ReaderActivityPayload payload = new ReaderActivityPayload(
                progressPayload.userId(),
                progressPayload.bookId(),
                "progress_updated",
                Map.of(
                        "sourceEvent", event.eventId().toString(),
                        "producer", event.producer(),
                        "page", String.valueOf(progressPayload.page()),
                        "progressPercent", String.valueOf(progressPayload.progressPercent())
                )
        );
        readerActivityRepository.save(event.eventId().toString(), payload);

        eventPublisher.publish(
                KafkaTopics.READER_ACTIVITY,
                EventType.READER_ACTIVITY_RECORDED,
                progressPayload.bookId(),
                payload,
                "activity-service"
        );
    }

    private ReaderProgressPayload toProgressPayload(DomainEvent event) {
        try {
            return objectMapper.treeToValue(event.payload(), ReaderProgressPayload.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid READER_PROGRESS payload", ex);
        }
    }
}
