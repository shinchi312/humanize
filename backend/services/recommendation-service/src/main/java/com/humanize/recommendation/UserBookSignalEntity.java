package com.humanize.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reco_user_book_signals")
public class UserBookSignalEntity {
    @Id
    @Column(name = "signal_key", nullable = false, length = 300)
    private String signalKey;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "book_id", nullable = false, length = 128)
    private String bookId;

    @Column(name = "last_progress_percent", nullable = false)
    private double lastProgressPercent;

    @Column(name = "last_page", nullable = false)
    private int lastPage;

    @Column(name = "activity_events", nullable = false)
    private int activityEvents;

    @Column(name = "last_activity_type", nullable = false, length = 64)
    private String lastActivityType;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getSignalKey() {
        return signalKey;
    }

    public void setSignalKey(String signalKey) {
        this.signalKey = signalKey;
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

    public double getLastProgressPercent() {
        return lastProgressPercent;
    }

    public void setLastProgressPercent(double lastProgressPercent) {
        this.lastProgressPercent = lastProgressPercent;
    }

    public int getLastPage() {
        return lastPage;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getActivityEvents() {
        return activityEvents;
    }

    public void setActivityEvents(int activityEvents) {
        this.activityEvents = activityEvents;
    }

    public String getLastActivityType() {
        return lastActivityType;
    }

    public void setLastActivityType(String lastActivityType) {
        this.lastActivityType = lastActivityType;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
