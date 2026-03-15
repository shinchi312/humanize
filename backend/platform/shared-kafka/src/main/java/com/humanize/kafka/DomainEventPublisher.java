package com.humanize.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisher.class);

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DomainEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String topic, EventType eventType, String partitionKey, Object payload, String producer) {
        DomainEvent event = DomainEvent.of(eventType, producer, partitionKey, objectMapper.valueToTree(payload));
        kafkaTemplate.send(topic, partitionKey, event);
        log.info("published eventType={} topic={} key={} producer={}", eventType, topic, partitionKey, producer);
    }
}
