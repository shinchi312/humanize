package com.humanize.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.events.DomainEvent;
import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.NotificationLifecyclePayload;
import com.humanize.contracts.payload.ReaderActivityPayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class NotificationEventFlow {
    private static final Logger log = LoggerFactory.getLogger(NotificationEventFlow.class);

    private final DomainEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final NotificationStateRepository stateRepository;
    private final NotificationEmailSender notificationEmailSender;

    public NotificationEventFlow(
            DomainEventPublisher eventPublisher,
            ObjectMapper objectMapper,
            NotificationStateRepository stateRepository,
            NotificationEmailSender notificationEmailSender
    ) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.stateRepository = stateRepository;
        this.notificationEmailSender = notificationEmailSender;
    }

    @KafkaListener(topics = KafkaTopics.READER_ACTIVITY, groupId = "notification-service")
    public void onReaderActivity(DomainEvent event) {
        ReaderActivityPayload activity = toPayload(event, ReaderActivityPayload.class);
        NotificationStateRepository.NotificationPreferenceView preference = stateRepository.preferences(activity.userId());
        if (!preference.spoilerEnabled() || !preference.emailEnabled()) {
            return;
        }

        double progressPercent = parseProgressPercent(activity);
        if (progressPercent < preference.spoilerMinProgressPercent()) {
            return;
        }
        if (!stateRepository.canRequestSpoiler(activity.userId(), activity.bookId())) {
            return;
        }

        NotificationLifecyclePayload payload = new NotificationLifecyclePayload(
                activity.userId(),
                activity.bookId(),
                "SPOILER_REQUESTED",
                "EMAIL",
                "Progress %.1f%% reached. Generate curiosity spoiler.".formatted(progressPercent)
        );

        stateRepository.recordLifecycle(
                payload.userId(),
                payload.bookId(),
                payload.status(),
                payload.channel(),
                payload.contentPreview(),
                "notification-service"
        );
        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_LIFECYCLE,
                EventType.NOTIFICATION_SPOILER_REQUESTED,
                activity.bookId(),
                payload,
                "notification-service"
        );
    }

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_LIFECYCLE, groupId = "notification-service")
    public void onNotificationLifecycle(DomainEvent event) {
        if (event.type() != EventType.NOTIFICATION_SPOILER_GENERATED) {
            return;
        }

        NotificationLifecyclePayload generated = toPayload(event, NotificationLifecyclePayload.class);
        String email = stateRepository.resolveTargetEmail(generated.userId());
        if (!StringUtils.hasText(email)) {
            stateRepository.recordLifecycle(
                    generated.userId(),
                    generated.bookId(),
                    "EMAIL_FAILED",
                    generated.channel(),
                    generated.contentPreview(),
                    "missing target email"
            );
            publishEmailLifecycle(EventType.NOTIFICATION_EMAIL_FAILED, generated, "missing target email");
            return;
        }

        NotificationEmailSender.SendResult sendResult = notificationEmailSender.sendSpoilerEmail(email, generated);
        String status = sendResult.success() ? "EMAIL_SENT" : "EMAIL_FAILED";
        stateRepository.recordLifecycle(
                generated.userId(),
                generated.bookId(),
                status,
                generated.channel(),
                generated.contentPreview(),
                sendResult.provider() + ":" + sendResult.detail()
        );
        publishEmailLifecycle(
                sendResult.success() ? EventType.NOTIFICATION_EMAIL_SENT : EventType.NOTIFICATION_EMAIL_FAILED,
                generated,
                sendResult.detail()
        );
        log.info("notification email outcome user={} book={} status={} detail={}",
                generated.userId(), generated.bookId(), status, sendResult.detail());
    }

    private void publishEmailLifecycle(EventType type, NotificationLifecyclePayload generated, String detail) {
        NotificationLifecyclePayload lifecyclePayload = new NotificationLifecyclePayload(
                generated.userId(),
                generated.bookId(),
                type == EventType.NOTIFICATION_EMAIL_SENT ? "EMAIL_SENT" : "EMAIL_FAILED",
                generated.channel(),
                generated.contentPreview() + " | " + detail
        );
        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_LIFECYCLE,
                type,
                generated.bookId(),
                lifecyclePayload,
                "notification-service"
        );
    }

    private double parseProgressPercent(ReaderActivityPayload activity) {
        if (activity.metadata() == null) {
            return 0.0;
        }
        String value = activity.metadata().get("progressPercent");
        if (!StringUtils.hasText(value)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0.0;
        }
    }

    private <T> T toPayload(DomainEvent event, Class<T> payloadClass) {
        try {
            return objectMapper.treeToValue(event.payload(), payloadClass);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid payload for " + payloadClass.getSimpleName(), ex);
        }
    }
}
