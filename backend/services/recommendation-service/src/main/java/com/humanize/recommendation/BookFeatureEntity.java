package com.humanize.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "reco_book_features")
public class BookFeatureEntity {
    @Id
    @Column(name = "book_id", nullable = false, length = 128)
    private String bookId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(nullable = false, length = 128)
    private String genre;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "extracted_chars", nullable = false)
    private int extractedChars;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
