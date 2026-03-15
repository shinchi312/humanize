package com.humanize.ingestion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.BookMetadataExtractedPayload;
import com.humanize.contracts.payload.BookProcessingPayload;
import com.humanize.contracts.payload.BookUploadedPayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookIngestionOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(BookIngestionOrchestrator.class);

    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final PdfSourceResolver pdfSourceResolver;
    private final LocalPdfTextExtractor localPdfTextExtractor;
    private final OcrFallbackExtractor ocrFallbackExtractor;
    private final IngestionProperties ingestionProperties;
    private final MetadataHeuristicService metadataHeuristicService;
    private final ProcessingStateRepository processingStateRepository;

    public BookIngestionOrchestrator(
            DomainEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            PdfSourceResolver pdfSourceResolver,
            LocalPdfTextExtractor localPdfTextExtractor,
            OcrFallbackExtractor ocrFallbackExtractor,
            IngestionProperties ingestionProperties,
            MetadataHeuristicService metadataHeuristicService,
            ProcessingStateRepository processingStateRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.pdfSourceResolver = pdfSourceResolver;
        this.localPdfTextExtractor = localPdfTextExtractor;
        this.ocrFallbackExtractor = ocrFallbackExtractor;
        this.ingestionProperties = ingestionProperties;
        this.metadataHeuristicService = metadataHeuristicService;
        this.processingStateRepository = processingStateRepository;
    }

    public void process(DomainEvent event) {
        BookUploadedPayload uploadedPayload = parsePayload(event);
        String bookId = uploadedPayload.bookId();

        processingStateRepository.save(BookProcessingState.started(bookId, "Ingestion started"));
        publishProcessingEvent(bookId, EventType.BOOK_PROCESSING_STARTED, "STARTED", "Text extraction started");

        try (ResolvedPdfSource source = pdfSourceResolver.resolve(uploadedPayload.objectKey())) {
            ExtractionResult extractionResult = extractText(source, uploadedPayload.objectKey());
            MetadataHeuristicService.Metadata metadata = metadataHeuristicService.infer(extractionResult.text());

            BookProcessingState completedState = new BookProcessingState(
                    bookId,
                    "COMPLETED",
                    "Book content extracted successfully",
                    extractionResult.source(),
                    extractionResult.extractedChars(),
                    metadata.title(),
                    metadata.author(),
                    metadata.genre(),
                    Instant.now()
            );
            processingStateRepository.save(completedState);
            eventPublisher.publish(
                    KafkaTopics.BOOK_PROCESSING,
                    EventType.BOOK_METADATA_EXTRACTED,
                    bookId,
                    new BookMetadataExtractedPayload(
                            bookId,
                            metadata.title(),
                            metadata.author(),
                            metadata.genre(),
                            extractionResult.source(),
                            extractionResult.extractedChars()
                    ),
                    "ingestion-service"
            );

            publishProcessingEvent(
                    bookId,
                    EventType.BOOK_PROCESSING_COMPLETED,
                    "COMPLETED",
                    "Ready. source=%s chars=%d".formatted(extractionResult.source(), extractionResult.extractedChars())
            );
        } catch (RuntimeException ex) {
            log.error("ingestion failed bookId={} message={}", bookId, ex.getMessage(), ex);
            processingStateRepository.save(BookProcessingState.failed(bookId, ex.getMessage()));
            publishProcessingEvent(bookId, EventType.BOOK_PROCESSING_FAILED, "FAILED", ex.getMessage());
        }
    }

    private ExtractionResult extractText(ResolvedPdfSource source, String objectKey) {
        Optional<String> textFromPdf = localPdfTextExtractor.extract(source.filePath());
        String text = textFromPdf.orElse("");
        String extractionSource = "PDF_TEXT";

        if (text.length() < ingestionProperties.getMinExtractedChars()) {
            Optional<String> textFromOcr = ocrFallbackExtractor.extract(source.filePath(), objectKey);
            if (textFromOcr.isPresent()) {
                text = textFromOcr.get();
                extractionSource = "OCR_FALLBACK";
            }
        }

        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(
                    "No extractable text found. Check source file availability or OCR setup."
            );
        }
        String sourceInfo = "%s+%s".formatted(source.sourceType(), extractionSource);
        return new ExtractionResult(text, sourceInfo);
    }

    private BookUploadedPayload parsePayload(DomainEvent event) {
        try {
            return objectMapper.treeToValue(event.payload(), BookUploadedPayload.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid BOOK_UPLOADED payload", ex);
        }
    }

    private void publishProcessingEvent(String bookId, EventType type, String status, String message) {
        eventPublisher.publish(
                KafkaTopics.BOOK_PROCESSING,
                type,
                bookId,
                new BookProcessingPayload(bookId, status, message),
                "ingestion-service"
        );
    }
}
