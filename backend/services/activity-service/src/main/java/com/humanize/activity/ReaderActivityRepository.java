package com.humanize.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.payload.ReaderActivityPayload;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class ReaderActivityRepository {
    private final ReaderActivityJpaRepository jpaRepository;
    private final ObjectMapper objectMapper;

    public ReaderActivityRepository(ReaderActivityJpaRepository jpaRepository, ObjectMapper objectMapper) {
        this.jpaRepository = jpaRepository;
        this.objectMapper = objectMapper;
    }

    public void save(String eventId, ReaderActivityPayload payload) {
        ReaderActivityEntity entity = new ReaderActivityEntity();
        entity.setEventId(eventId);
        entity.setUserId(payload.userId());
        entity.setBookId(payload.bookId());
        entity.setActivityType(payload.activityType());
        entity.setMetadataJson(toJson(payload.metadata()));
        entity.setCreatedAt(Instant.now());
        jpaRepository.save(entity);
    }

    public List<Map<String, Object>> findByUser(String userId) {
        return jpaRepository.findTop100ByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toMap).toList();
    }

    public List<Map<String, Object>> findByBook(String bookId) {
        return jpaRepository.findTop100ByBookIdOrderByCreatedAtDesc(bookId).stream().map(this::toMap).toList();
    }

    private Map<String, Object> toMap(ReaderActivityEntity entity) {
        return Map.of(
                "eventId", entity.getEventId(),
                "userId", entity.getUserId(),
                "bookId", entity.getBookId(),
                "activityType", entity.getActivityType(),
                "metadata", fromJson(entity.getMetadataJson()),
                "createdAt", entity.getCreatedAt().toString()
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> fromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception ex) {
            return Map.of("raw", json);
        }
    }

    private String toJson(Map<String, String> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception ex) {
            return "{\"error\":\"metadata_serialization_failed\"}";
        }
    }
}
