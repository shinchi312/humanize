package com.humanize.reader;

import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.ReaderProgressPayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reader")
public class ReaderProgressController {

    private final DomainEventPublisher eventPublisher;
    private final ReaderProgressRepository readerProgressRepository;

    public ReaderProgressController(
            DomainEventPublisher eventPublisher,
            ReaderProgressRepository readerProgressRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.readerProgressRepository = readerProgressRepository;
    }

    @PostMapping("/progress")
    public Map<String, Object> updateProgress(@Valid @RequestBody ReaderProgressPayload payload) {
        ReaderProgressPayload saved = readerProgressRepository.save(payload);

        eventPublisher.publish(
                KafkaTopics.READER_PROGRESS,
                EventType.READER_PROGRESS_UPDATED,
                saved.bookId(),
                saved,
                "reader-service"
        );

        return Map.of(
                "status", "accepted",
                "bookId", saved.bookId(),
                "progressPercent", saved.progressPercent()
        );
    }

    @GetMapping("/progress/{userId}/{bookId}")
    public Map<String, Object> getProgress(@PathVariable String userId, @PathVariable String bookId) {
        return readerProgressRepository.findByUserAndBook(userId, bookId)
                .<Map<String, Object>>map(payload -> Map.of(
                        "status", "found",
                        "progress", payload
                ))
                .orElse(Map.of(
                        "status", "not_found",
                        "message", "No saved progress for this user/book"
                ));
    }

    @GetMapping("/progress/user/{userId}")
    public Map<String, Object> getUserProgress(@PathVariable String userId) {
        List<ReaderProgressPayload> progress = readerProgressRepository.findByUser(userId);
        return Map.of(
                "userId", userId,
                "count", progress.size(),
                "progress", progress
        );
    }
}
