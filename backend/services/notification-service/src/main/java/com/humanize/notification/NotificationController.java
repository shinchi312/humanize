package com.humanize.notification;

import com.humanize.contracts.events.EventType;
import com.humanize.contracts.payload.NotificationLifecyclePayload;
import com.humanize.kafka.DomainEventPublisher;
import com.humanize.kafka.KafkaTopics;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final DomainEventPublisher eventPublisher;
    private final NotificationStateRepository stateRepository;

    public NotificationController(
            DomainEventPublisher eventPublisher,
            NotificationStateRepository stateRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.stateRepository = stateRepository;
    }

    @PostMapping("/email/sent")
    public Map<String, Object> markEmailSent(@Valid @RequestBody NotificationLifecyclePayload payload) {
        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_LIFECYCLE,
                EventType.NOTIFICATION_EMAIL_SENT,
                payload.bookId(),
                payload,
                "notification-service"
        );

        return Map.of("status", "recorded", "bookId", payload.bookId(), "channel", payload.channel());
    }

    @GetMapping("/preferences/{userId}")
    public NotificationStateRepository.NotificationPreferenceView getPreferences(@PathVariable String userId) {
        return stateRepository.preferences(userId);
    }

    @GetMapping("/logs/{userId}")
    public Map<String, Object> logs(
            @PathVariable String userId,
            @RequestParam(name = "bookId", required = false) String bookId
    ) {
        var logs = stateRepository.recentLogs(userId, bookId);
        return Map.of(
                "userId", userId,
                "bookId", bookId == null ? "" : bookId,
                "count", logs.size(),
                "logs", logs
        );
    }

    @PutMapping("/preferences/{userId}")
    public NotificationStateRepository.NotificationPreferenceView upsertPreferences(
            @PathVariable String userId,
            @Valid @RequestBody NotificationPreferenceRequest request
    ) {
        return stateRepository.upsertPreferences(
                userId,
                new NotificationStateRepository.PreferenceUpsertRequest(
                        request.emailEnabled(),
                        request.emailAddress(),
                        request.spoilerEnabled(),
                        request.spoilerMinProgressPercent()
                )
        );
    }

    @PostMapping("/spoiler/request")
    public Map<String, Object> requestSpoiler(@Valid @RequestBody SpoilerRequest request) {
        NotificationLifecyclePayload payload = new NotificationLifecyclePayload(
                request.userId(),
                request.bookId(),
                "SPOILER_REQUESTED",
                "EMAIL",
                request.reason()
        );
        stateRepository.recordLifecycle(
                payload.userId(),
                payload.bookId(),
                payload.status(),
                payload.channel(),
                payload.contentPreview(),
                "manual_request"
        );
        eventPublisher.publish(
                KafkaTopics.NOTIFICATION_LIFECYCLE,
                EventType.NOTIFICATION_SPOILER_REQUESTED,
                request.bookId(),
                payload,
                "notification-service"
        );
        return Map.of(
                "status", "requested",
                "userId", request.userId(),
                "bookId", request.bookId(),
                "channel", "EMAIL"
        );
    }

    public record NotificationPreferenceRequest(
            boolean emailEnabled,
            @Email String emailAddress,
            boolean spoilerEnabled,
            @DecimalMin("1.0") @DecimalMax("100.0") double spoilerMinProgressPercent
    ) {
    }

    public record SpoilerRequest(
            @NotBlank String userId,
            @NotBlank String bookId,
            @NotBlank String reason
    ) {
    }
}
