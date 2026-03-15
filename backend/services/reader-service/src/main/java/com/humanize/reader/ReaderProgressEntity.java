package com.humanize.reader;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reader_progress_state")
public class ReaderProgressEntity {
    @Id
    @Column(name = "progress_key", nullable = false, length = 260)
    private String progressKey;

    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "book_id", nullable = false, length = 128)
    private String bookId;

    @Column(nullable = false)
    private int page;

    @Column(name = "progress_percent", nullable = false)
    private double progressPercent;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getProgressKey() {
        return progressKey;
    }

    public void setProgressKey(String progressKey) {
        this.progressKey = progressKey;
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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
