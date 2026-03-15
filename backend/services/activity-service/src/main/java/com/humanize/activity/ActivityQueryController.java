package com.humanize.activity;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityQueryController {
    private final ReaderActivityRepository readerActivityRepository;

    public ActivityQueryController(ReaderActivityRepository readerActivityRepository) {
        this.readerActivityRepository = readerActivityRepository;
    }

    @GetMapping("/users/{userId}/events")
    public Map<String, Object> userEvents(@PathVariable String userId) {
        var events = readerActivityRepository.findByUser(userId);
        return Map.of(
                "userId", userId,
                "count", events.size(),
                "events", events
        );
    }

    @GetMapping("/books/{bookId}/events")
    public Map<String, Object> bookEvents(@PathVariable String bookId) {
        var events = readerActivityRepository.findByBook(bookId);
        return Map.of(
                "bookId", bookId,
                "count", events.size(),
                "events", events
        );
    }
}
