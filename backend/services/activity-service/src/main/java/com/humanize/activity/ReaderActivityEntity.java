package com.humanize.activity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reader_activity_events")
public class ReaderActivityEntity {
    @Id
    @Column(name = "event_id", nullable = false, length = 128)
    private String eventId;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "book_id", nullable = false, length = 128)
    private String bookId;

    @Column(name = "activity_type", nullable = false, length = 64)
    private String activityType;

    @Column(name = "metadata_json", nullable = false, length = 4000)
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
