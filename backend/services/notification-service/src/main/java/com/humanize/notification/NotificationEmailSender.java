package com.humanize.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.humanize.contracts.payload.NotificationLifecyclePayload;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationEmailSender {
    private static final Logger log = LoggerFactory.getLogger(NotificationEmailSender.class);

    private final NotificationProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public NotificationEmailSender(NotificationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public SendResult sendSpoilerEmail(String toEmail, NotificationLifecyclePayload payload) {
        String subject = "Your next spoiler from Humanize";
        String body = payload.contentPreview();

        if (!StringUtils.hasText(properties.getEmail().getApiKey())) {
            log.info("brevo api key missing, sender in log-only mode to={} bookId={} preview={}",
                    toEmail, payload.bookId(), body);
            return new SendResult(true, "log-only", "email logged");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(buildBrevoPayload(toEmail, subject, body));

            String endpoint = normalizeBaseUrl(properties.getEmail().getBrevoBaseUrl()) + "/v3/smtp/email";
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("api-key", properties.getEmail().getApiKey().trim())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody));

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new SendResult(true, "brevo", "status=" + response.statusCode() + " " + extractMessageId(response.body()));
            }
            return new SendResult(false, "brevo", "status=" + response.statusCode() + " body=" + truncate(response.body(), 180));
        } catch (Exception ex) {
            return new SendResult(false, "brevo", ex.getMessage());
        }
    }

    private Map<String, Object> buildBrevoPayload(String toEmail, String subject, String textContent) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sender", Map.of(
                "name", defaultValue(properties.getEmail().getFromName(), "Humanize"),
                "email", defaultValue(properties.getEmail().getFromEmail(), "no-reply@humanize.local")
        ));
        payload.put("to", List.of(Map.of("email", toEmail)));
        payload.put("subject", subject);
        payload.put("textContent", textContent);

        if (StringUtils.hasText(properties.getEmail().getReplyToEmail())) {
            Map<String, String> replyTo = new LinkedHashMap<>();
            replyTo.put("email", properties.getEmail().getReplyToEmail().trim());
            if (StringUtils.hasText(properties.getEmail().getReplyToName())) {
                replyTo.put("name", properties.getEmail().getReplyToName().trim());
            }
            payload.put("replyTo", replyTo);
        }
        return payload;
    }

    private static String normalizeBaseUrl(String url) {
        String fallback = "https://api.brevo.com";
        String raw = StringUtils.hasText(url) ? url.trim() : fallback;
        return raw.replaceAll("/+$", "");
    }

    private String extractMessageId(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String messageId = root.path("messageId").asText("");
            if (StringUtils.hasText(messageId)) {
                return "messageId=" + messageId;
            }
        } catch (Exception ignored) {
            // Ignore parsing issues for provider response metadata.
        }
        return "";
    }

    private static String truncate(String value, int maxChars) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars);
    }

    private static String defaultValue(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    public record SendResult(boolean success, String provider, String detail) {
    }
}
