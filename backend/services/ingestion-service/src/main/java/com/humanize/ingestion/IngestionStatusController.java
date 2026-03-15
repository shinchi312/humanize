package com.humanize.ingestion;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingestion/books")
public class IngestionStatusController {

    private final ProcessingStateRepository processingStateRepository;

    public IngestionStatusController(ProcessingStateRepository processingStateRepository) {
        this.processingStateRepository = processingStateRepository;
    }

    @GetMapping("/{bookId}/status")
    public Map<String, Object> status(@PathVariable String bookId) {
        return processingStateRepository.findByBookId(bookId)
                .map(state -> Map.<String, Object>of(
                        "bookId", state.bookId(),
                        "status", state.status(),
                        "message", state.message(),
                        "source", state.source(),
                        "extractedChars", state.extractedChars(),
                        "title", state.title(),
                        "author", state.author(),
                        "genre", state.genre(),
                        "updatedAt", state.updatedAt().toString()
                ))
                .orElse(Map.of(
                        "bookId", bookId,
                        "status", "NOT_FOUND",
                        "message", "No ingestion state recorded yet"
                ));
    }
}
