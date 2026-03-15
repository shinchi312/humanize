package com.humanize.ingestion;

import com.humanize.contracts.events.DomainEvent;
import com.humanize.kafka.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookIngestionListener {
    private final BookIngestionOrchestrator ingestionOrchestrator;

    public BookIngestionListener(BookIngestionOrchestrator ingestionOrchestrator) {
        this.ingestionOrchestrator = ingestionOrchestrator;
    }

    @KafkaListener(topics = KafkaTopics.BOOK_UPLOADED, groupId = "ingestion-service")
    public void onBookUploaded(DomainEvent event) {
        ingestionOrchestrator.process(event);
    }
}
