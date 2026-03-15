package com.humanize.contracts.events;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        EventType type,
        Instant occurredAt,
        String producer,
        String partitionKey,
        JsonNode payload,
        int schemaVersion
) {
    public static DomainEvent of(EventType type, String producer, String partitionKey, JsonNode payload) {
        return new DomainEvent(
                UUID.randomUUID(),
                type,
                Instant.now(),
                producer,
                partitionKey,
                payload,
                1
        );
    }
}
