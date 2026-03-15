package com.humanize.library;

import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.BookUploadedPayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library/books")
public class BookUploadController {

    private final DomainEventPublisher eventPublisher;
    private final R2UploadIntentService uploadIntentService;
    private final LibraryStateRepository libraryStateRepository;

    public BookUploadController(
            DomainEventPublisher eventPublisher,
            R2UploadIntentService uploadIntentService,
            LibraryStateRepository libraryStateRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.uploadIntentService = uploadIntentService;
        this.libraryStateRepository = libraryStateRepository;
    }

    @PostMapping("/upload-intent")
    public UploadIntentResponse createUploadIntent(@Valid @RequestBody UploadIntentRequest request) {
        R2UploadIntentService.UploadIntent intent = uploadIntentService.createIntent(
                new R2UploadIntentService.UploadIntentInput(
                        request.userId(),
                        request.bookId(),
                        request.fileName(),
                        request.contentType()
                )
        );
        libraryStateRepository.recordUploadIntent(
                request.userId(),
                request.bookId(),
                request.fileName(),
                intent.objectKey(),
                request.contentType()
        );

        return new UploadIntentResponse(
                request.bookId(),
                intent.objectKey(),
                intent.uploadUrl(),
                intent.requiredHeaders(),
                intent.expiresAt()
        );
    }

    @PostMapping("/uploaded")
    public Map<String, Object> markUploaded(@Valid @RequestBody BookUploadedRequest request) {
        libraryStateRepository.markUploaded(
                request.userId(),
                request.bookId(),
                request.objectKey(),
                request.contentType()
        );

        BookUploadedPayload payload = new BookUploadedPayload(
                request.bookId(),
                request.userId(),
                request.objectKey(),
                request.contentType()
        );

        eventPublisher.publish(
                KafkaTopics.BOOK_UPLOADED,
                EventType.BOOK_UPLOADED,
                request.bookId(),
                payload,
                "library-service"
        );

        return Map.of(
                "status", "accepted",
                "topic", KafkaTopics.BOOK_UPLOADED,
                "bookId", request.bookId()
        );
    }

    @GetMapping("/user/{userId}")
    public Map<String, Object> userLibrary(@PathVariable String userId) {
        List<Map<String, Object>> books = libraryStateRepository.userLibrary(userId);
        return Map.of(
                "userId", userId,
                "count", books.size(),
                "books", books
        );
    }

    public record BookUploadedRequest(
            @NotBlank String bookId,
            @NotBlank String userId,
            @NotBlank String objectKey,
            @NotBlank String contentType
    ) {
    }

    public record UploadIntentRequest(
            @NotBlank String userId,
            @NotBlank String bookId,
            @NotBlank String fileName,
            @NotBlank String contentType
    ) {
    }

    public record UploadIntentResponse(
            String bookId,
            String objectKey,
            String uploadUrl,
            Map<String, String> requiredHeaders,
            String expiresAt
    ) {
    }
}
