package com.humanize.recommendation;

import com.humanize.contracts.payload.BookMetadataExtractedPayload;
import com.humanize.contracts.payload.ReaderActivityPayload;
import com.humanize.contracts.payload.ReaderProgressPayload;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RecommendationScoringService {
    private final BookFeatureJpaRepository featureRepository;
    private final UserBookSignalJpaRepository signalRepository;
    private final RecommendationScoreJpaRepository scoreRepository;

    public RecommendationScoringService(
            BookFeatureJpaRepository featureRepository,
            UserBookSignalJpaRepository signalRepository,
            RecommendationScoreJpaRepository scoreRepository
    ) {
        this.featureRepository = featureRepository;
        this.signalRepository = signalRepository;
        this.scoreRepository = scoreRepository;
    }

    public void applyMetadata(BookMetadataExtractedPayload payload) {
        BookFeatureEntity feature = featureRepository.findById(payload.bookId()).orElseGet(BookFeatureEntity::new);
        feature.setBookId(payload.bookId());
        feature.setTitle(safe(payload.title(), "Untitled Book"));
        feature.setAuthor(safe(payload.author(), "Unknown Author"));
        feature.setGenre(safe(payload.genre(), "General"));
        feature.setSource(safe(payload.source(), "UNKNOWN"));
        feature.setExtractedChars(payload.extractedChars());
        feature.setUpdatedAt(Instant.now());
        featureRepository.save(feature);

        signalRepository.findByBookId(payload.bookId()).forEach(this::upsertScore);
    }

    public void applyProgress(ReaderProgressPayload payload) {
        UserBookSignalEntity signal = signalRepository.findById(key(payload.userId(), payload.bookId()))
                .orElseGet(UserBookSignalEntity::new);
        signal.setSignalKey(key(payload.userId(), payload.bookId()));
        signal.setUserId(payload.userId());
        signal.setBookId(payload.bookId());
        signal.setLastPage(payload.page());
        signal.setLastProgressPercent(payload.progressPercent());
        signal.setUpdatedAt(Instant.now());
        if (!StringUtils.hasText(signal.getLastActivityType())) {
            signal.setLastActivityType("progress_updated");
        }
        signalRepository.save(signal);
        upsertScore(signal);
    }

    public void applyActivity(ReaderActivityPayload payload) {
        UserBookSignalEntity signal = signalRepository.findById(key(payload.userId(), payload.bookId()))
                .orElseGet(UserBookSignalEntity::new);
        signal.setSignalKey(key(payload.userId(), payload.bookId()));
        signal.setUserId(payload.userId());
        signal.setBookId(payload.bookId());
        signal.setActivityEvents(signal.getActivityEvents() + 1);
        signal.setLastActivityType(safe(payload.activityType(), "activity"));
        signal.setUpdatedAt(Instant.now());
        signalRepository.save(signal);
        upsertScore(signal);
    }

    public List<Map<String, Object>> topRecommendations(String userId) {
        return scoreRepository.findTop20ByUserIdOrderByScoreDescUpdatedAtDesc(userId).stream()
                .map(score -> {
                    BookFeatureEntity feature = featureRepository.findById(score.getBookId()).orElse(null);
                    return Map.<String, Object>of(
                            "bookId", score.getBookId(),
                            "score", score.getScore(),
                            "reason", score.getReason(),
                            "title", feature == null ? "Unknown title" : feature.getTitle(),
                            "author", feature == null ? "Unknown author" : feature.getAuthor(),
                            "genre", feature == null ? "General" : feature.getGenre()
                    );
                })
                .toList();
    }

    private void upsertScore(UserBookSignalEntity signal) {
        BookFeatureEntity feature = featureRepository.findById(signal.getBookId()).orElse(null);
        if (feature == null) {
            return;
        }

        double progressPart = clamp(signal.getLastProgressPercent(), 0.0, 100.0) * 0.70;
        double activityPart = Math.min(signal.getActivityEvents() * 4.0, 20.0);
        double metadataPart = Math.min(feature.getExtractedChars() / 250.0, 12.0);
        if (!"General".equalsIgnoreCase(feature.getGenre())) {
            metadataPart += 5.0;
        }
        if (feature.getSource().contains("OCR")) {
            metadataPart += 1.5;
        }
        double scoreValue = round2(progressPart + activityPart + metadataPart);

        RecommendationScoreEntity score = scoreRepository.findById(key(signal.getUserId(), signal.getBookId()))
                .orElseGet(RecommendationScoreEntity::new);
        score.setScoreKey(key(signal.getUserId(), signal.getBookId()));
        score.setUserId(signal.getUserId());
        score.setBookId(signal.getBookId());
        score.setScore(scoreValue);
        score.setReason(
                "progress=%.1f activity=%d genre=%s extractedChars=%d".formatted(
                        signal.getLastProgressPercent(),
                        signal.getActivityEvents(),
                        feature.getGenre(),
                        feature.getExtractedChars()
                )
        );
        score.setUpdatedAt(Instant.now());
        scoreRepository.save(score);
    }

    private static String key(String userId, String bookId) {
        return userId + "::" + bookId;
    }

    private static String safe(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
