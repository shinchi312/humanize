package com.humanize.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reco_scores")
public class RecommendationScoreEntity {
    @Id
    @Column(name = "score_key", nullable = false, length = 300)
    private String scoreKey;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "book_id", nullable = false, length = 128)
    private String bookId;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getScoreKey() {
        return scoreKey;
    }

    public void setScoreKey(String scoreKey) {
        this.scoreKey = scoreKey;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
