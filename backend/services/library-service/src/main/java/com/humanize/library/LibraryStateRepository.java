package com.humanize.library;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class LibraryStateRepository {
    private final LibraryBookJpaRepository bookRepository;
    private final LibraryUserBookJpaRepository userBookRepository;

    public LibraryStateRepository(
            LibraryBookJpaRepository bookRepository,
            LibraryUserBookJpaRepository userBookRepository
    ) {
        this.bookRepository = bookRepository;
        this.userBookRepository = userBookRepository;
    }

    public void recordUploadIntent(String userId, String bookId, String fileName, String objectKey, String contentType) {
        Instant now = Instant.now();

        LibraryBookEntity book = bookRepository.findById(bookId).orElseGet(LibraryBookEntity::new);
        book.setBookId(bookId);
        book.setObjectKey(objectKey);
        book.setContentType(contentType);
        book.setStatus("UPLOAD_INTENT_CREATED");
        if (book.getCreatedAt() == null) {
            book.setCreatedAt(now);
        }
        book.setUpdatedAt(now);
        bookRepository.save(book);

        LibraryUserBookEntity mapping = userBookRepository.findById(mappingKey(userId, bookId)).orElseGet(LibraryUserBookEntity::new);
        mapping.setMappingKey(mappingKey(userId, bookId));
        mapping.setUserId(userId);
        mapping.setBookId(bookId);
        mapping.setFileName(fileName);
        mapping.setPublic(false);
        if (mapping.getCreatedAt() == null) {
            mapping.setCreatedAt(now);
        }
        mapping.setUpdatedAt(now);
        userBookRepository.save(mapping);
    }

    public void markUploaded(String userId, String bookId, String objectKey, String contentType) {
        Instant now = Instant.now();

        LibraryBookEntity book = bookRepository.findById(bookId).orElseGet(LibraryBookEntity::new);
        book.setBookId(bookId);
        book.setObjectKey(objectKey);
        book.setContentType(contentType);
        book.setStatus("UPLOADED");
        if (book.getCreatedAt() == null) {
            book.setCreatedAt(now);
        }
        book.setUpdatedAt(now);
        bookRepository.save(book);

        LibraryUserBookEntity mapping = userBookRepository.findById(mappingKey(userId, bookId)).orElseGet(LibraryUserBookEntity::new);
        mapping.setMappingKey(mappingKey(userId, bookId));
        mapping.setUserId(userId);
        mapping.setBookId(bookId);
        if (mapping.getFileName() == null || mapping.getFileName().isBlank()) {
            mapping.setFileName(bookId + ".pdf");
        }
        mapping.setPublic(mapping.isPublic());
        if (mapping.getCreatedAt() == null) {
            mapping.setCreatedAt(now);
        }
        mapping.setUpdatedAt(now);
        userBookRepository.save(mapping);
    }

    public List<Map<String, Object>> userLibrary(String userId) {
        return userBookRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(mapping -> {
                    LibraryBookEntity book = bookRepository.findById(mapping.getBookId()).orElse(null);
                    return Map.<String, Object>of(
                            "userId", mapping.getUserId(),
                            "bookId", mapping.getBookId(),
                            "fileName", mapping.getFileName(),
                            "isPublic", mapping.isPublic(),
                            "status", book == null ? "UNKNOWN" : book.getStatus(),
                            "contentType", book == null ? "unknown" : book.getContentType(),
                            "objectKey", book == null ? "" : book.getObjectKey(),
                            "updatedAt", (book == null ? mapping.getUpdatedAt() : book.getUpdatedAt()).toString()
                    );
                })
                .toList();
    }

    private static String mappingKey(String userId, String bookId) {
        return userId + "::" + bookId;
    }
}
