package com.humanize.ingestion;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ProcessingStateRepository {
    private final BookProcessingStateJpaRepository jpaRepository;

    public ProcessingStateRepository(BookProcessingStateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public void save(BookProcessingState state) {
        jpaRepository.save(toEntity(state));
    }

    public Optional<BookProcessingState> findByBookId(String bookId) {
        return jpaRepository.findById(bookId).map(this::toRecord);
    }

    public Collection<BookProcessingState> findAll() {
        return jpaRepository.findAll().stream().map(this::toRecord).toList();
    }

    private BookProcessingStateEntity toEntity(BookProcessingState state) {
        BookProcessingStateEntity entity = new BookProcessingStateEntity();
        entity.setBookId(state.bookId());
        entity.setStatus(state.status());
        entity.setMessage(state.message());
        entity.setSource(state.source());
        entity.setExtractedChars(state.extractedChars());
        entity.setTitle(state.title());
        entity.setAuthor(state.author());
        entity.setGenre(state.genre());
        entity.setUpdatedAt(state.updatedAt());
        return entity;
    }

    private BookProcessingState toRecord(BookProcessingStateEntity entity) {
        return new BookProcessingState(
                entity.getBookId(),
                safe(entity.getStatus(), "UNKNOWN"),
                safe(entity.getMessage(), ""),
                safe(entity.getSource(), "N/A"),
                entity.getExtractedChars(),
                safe(entity.getTitle(), ""),
                safe(entity.getAuthor(), ""),
                safe(entity.getGenre(), ""),
                entity.getUpdatedAt() == null ? Instant.now() : entity.getUpdatedAt()
        );
    }

    private static String safe(String value, String fallback) {
        return value == null ? fallback : value;
    }
}
