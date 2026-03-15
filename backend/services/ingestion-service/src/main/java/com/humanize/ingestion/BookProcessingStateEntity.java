package com.humanize.ingestion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ingestion_processing_state")
public class BookProcessingStateEntity {
    @Id
    @Column(name = "book_id", nullable = false, length = 128)
    private String bookId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "extracted_chars", nullable = false)
    private int extractedChars;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false, length = 128)
    private String genre;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getExtractedChars() {
        return extractedChars;
    }

    public void setExtractedChars(int extractedChars) {
        this.extractedChars = extractedChars;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
