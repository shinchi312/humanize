package com.humanize.notification;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class NotificationStateRepository {
    private final NotificationPreferenceJpaRepository preferenceRepository;
    private final NotificationDeliveryLogJpaRepository deliveryLogRepository;
    private final NotificationProperties properties;

    public NotificationStateRepository(
            NotificationPreferenceJpaRepository preferenceRepository,
            NotificationDeliveryLogJpaRepository deliveryLogRepository,
            NotificationProperties properties
    ) {
        this.preferenceRepository = preferenceRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.properties = properties;
    }

    public NotificationPreferenceView preferences(String userId) {
        NotificationPreferenceEntity entity = preferenceRepository.findById(userId).orElse(null);
        if (entity == null) {
            return defaultPreference(userId);
        }
        return new NotificationPreferenceView(
                userId,
                entity.isEmailEnabled(),
                defaultEmail(entity.getEmailAddress(), userId),
                entity.isSpoilerEnabled(),
                entity.getSpoilerMinProgressPercent(),
                entity.getPreferredChannel(),
                entity.getUpdatedAt()
        );
    }

    public NotificationPreferenceView upsertPreferences(String userId, PreferenceUpsertRequest request) {
        NotificationPreferenceEntity entity = preferenceRepository.findById(userId).orElseGet(NotificationPreferenceEntity::new);
        entity.setUserId(userId);
        entity.setEmailEnabled(request.emailEnabled());
        entity.setEmailAddress(normalizeEmail(request.emailAddress(), userId));
        entity.setSpoilerEnabled(request.spoilerEnabled());
        entity.setSpoilerMinProgressPercent(clampProgress(request.spoilerMinProgressPercent()));
        entity.setPreferredChannel("EMAIL");
        entity.setUpdatedAt(Instant.now());
        preferenceRepository.save(entity);
        return preferences(userId);
    }

    public boolean canRequestSpoiler(String userId, String bookId) {
        long minutes = Math.max(1, properties.getSpoilerCooldownMinutes());
        Instant threshold = Instant.now().minusSeconds(minutes * 60);
        long recentCount = deliveryLogRepository.countByUserIdAndBookIdAndStatusAndCreatedAtAfter(
                userId,
                bookId,
                "SPOILER_REQUESTED",
                threshold
        );
        return recentCount == 0;
    }

    public void recordLifecycle(
            String userId,
            String bookId,
            String status,
            String channel,
            String contentPreview,
            String providerDetail
    ) {
        NotificationDeliveryLogEntity log = new NotificationDeliveryLogEntity();
        log.setUserId(userId);
        log.setBookId(bookId);
        log.setStatus(status);
        log.setChannel(channel);
        log.setContentPreview(truncate(contentPreview, 1100));
        log.setProviderDetail(truncate(providerDetail, 500));
        log.setCreatedAt(Instant.now());
        deliveryLogRepository.save(log);
    }

    public String resolveTargetEmail(String userId) {
        NotificationPreferenceView preference = preferences(userId);
        if (!preference.emailEnabled()) {
            return "";
        }
        return preference.emailAddress();
    }

    public List<DeliveryLogView> recentLogs(String userId, String bookId) {
        List<NotificationDeliveryLogEntity> logs;
        if (StringUtils.hasText(bookId)) {
            logs = deliveryLogRepository.findTop20ByUserIdAndBookIdOrderByCreatedAtDesc(userId, bookId);
        } else {
            logs = deliveryLogRepository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
        }
        return logs.stream()
                .map(log -> new DeliveryLogView(
                        log.getId(),
                        log.getUserId(),
                        log.getBookId(),
                        log.getStatus(),
                        log.getChannel(),
                        log.getContentPreview(),
                        log.getProviderDetail(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    private NotificationPreferenceView defaultPreference(String userId) {
        return new NotificationPreferenceView(
                userId,
                true,
                defaultEmail("", userId),
                true,
                properties.getDefaultSpoilerProgressPercent(),
                "EMAIL",
                Instant.now()
        );
    }

    private static String normalizeEmail(String emailAddress, String userId) {
        if (StringUtils.hasText(emailAddress)) {
            return emailAddress.trim();
        }
        return defaultEmail("", userId);
    }

    private static String defaultEmail(String current, String userId) {
        if (StringUtils.hasText(current)) {
            return current.trim();
        }
        String sanitizedUser = userId.replaceAll("[^a-zA-Z0-9._-]", "-");
        return sanitizedUser + "@example.com";
    }

    private static double clampProgress(double progress) {
        return Math.max(1.0, Math.min(progress, 100.0));
    }

    private static String truncate(String value, int max) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String clean = value.trim();
        if (clean.length() <= max) {
            return clean;
        }
        return clean.substring(0, max);
    }

    public record PreferenceUpsertRequest(
            boolean emailEnabled,
            String emailAddress,
            boolean spoilerEnabled,
            double spoilerMinProgressPercent
    ) {
    }

    public record NotificationPreferenceView(
            String userId,
            boolean emailEnabled,
            String emailAddress,
            boolean spoilerEnabled,
            double spoilerMinProgressPercent,
            String preferredChannel,
            Instant updatedAt
    ) {
    }

    public record DeliveryLogView(
            Long id,
            String userId,
            String bookId,
            String status,
            String channel,
            String contentPreview,
            String providerDetail,
            Instant createdAt
    ) {
    }
}
