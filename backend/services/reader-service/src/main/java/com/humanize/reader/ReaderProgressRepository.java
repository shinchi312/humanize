package com.humanize.reader;

import com.humanize.contracts.payload.ReaderProgressPayload;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ReaderProgressRepository {
    private final ReaderProgressJpaRepository jpaRepository;

    public ReaderProgressRepository(ReaderProgressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    public ReaderProgressPayload save(ReaderProgressPayload payload) {
        ReaderProgressEntity entity = new ReaderProgressEntity();
        entity.setProgressKey(key(payload.userId(), payload.bookId()));
        entity.setUserId(payload.userId());
        entity.setBookId(payload.bookId());
        entity.setPage(payload.page());
        entity.setProgressPercent(payload.progressPercent());
        entity.setUpdatedAt(Instant.now());
        jpaRepository.save(entity);
        return toPayload(entity);
    }

    public Optional<ReaderProgressPayload> findByUserAndBook(String userId, String bookId) {
        return jpaRepository.findById(key(userId, bookId)).map(this::toPayload);
    }

    public List<ReaderProgressPayload> findByUser(String userId) {
        return jpaRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toPayload).toList();
    }

    private ReaderProgressPayload toPayload(ReaderProgressEntity entity) {
        return new ReaderProgressPayload(
                entity.getUserId(),
                entity.getBookId(),
                entity.getPage(),
                entity.getProgressPercent()
        );
    }

    private static String key(String userId, String bookId) {
        return userId + "::" + bookId;
    }
}
