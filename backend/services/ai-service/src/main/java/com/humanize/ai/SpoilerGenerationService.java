package com.humanize.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.humanize.contracts.payload.NotificationLifecyclePayload;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SpoilerGenerationService {
    private static final Logger log = LoggerFactory.getLogger(SpoilerGenerationService.class);

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public SpoilerGenerationService(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    }

    public GeneratedSpoiler generate(NotificationLifecyclePayload request) {
        String generatedText = generateText(request);
        return new GeneratedSpoiler(trim(generatedText), aiProperties.getProvider(), aiProperties.getModel());
    }

    public GeneratedSpoiler previewFromCharacter(String bookId, String characterName) {
        NotificationLifecyclePayload pseudoRequest = new NotificationLifecyclePayload(
                "preview-user",
                bookId,
                "PREVIEW",
                "EMAIL",
                "Character focus: " + characterName
        );
        return generate(pseudoRequest);
    }

    private String generateText(NotificationLifecyclePayload request) {
        if ("ollama".equalsIgnoreCase(aiProperties.getProvider())) {
            String ollamaText = generateWithOllama(request);
            if (StringUtils.hasText(ollamaText)) {
                return ollamaText;
            }
            log.warn("ollama generation failed; fallback heuristic will be used");
        }
        return heuristicSpoiler(request);
    }

    private String generateWithOllama(NotificationLifecyclePayload request) {
        try {
            String prompt = """
                    Create a short spoiler teaser in 2 lines.
                    Keep it intense but not explicit about the ending.
                    Book ID: %s
                    Context: %s
                    """.formatted(request.bookId(), request.contentPreview());

            String body = objectMapper.writeValueAsString(Map.of(
                    "model", aiProperties.getModel(),
                    "prompt", prompt,
                    "stream", false
            ));
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(aiProperties.getOllamaBaseUrl().replaceAll("/+$", "") + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return "";
            }
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("response").asText("");
        } catch (Exception ex) {
            log.warn("ollama call failed: {}", ex.getMessage());
            return "";
        }
    }

    private String heuristicSpoiler(NotificationLifecyclePayload request) {
        String context = StringUtils.hasText(request.contentPreview())
                ? request.contentPreview()
                : "your reading pace has reached a turning point";
        return "You are closer than you think. " + context
                + " Behind the next chapter, an ally and a betrayal collide.";
    }

    private String trim(String value) {
        if (!StringUtils.hasText(value)) {
            return "A major turning point is one chapter away.";
        }
        String clean = value.trim();
        int max = Math.max(80, aiProperties.getMaxPreviewChars());
        if (clean.length() <= max) {
            return clean;
        }
        return clean.substring(0, max);
    }

    public record GeneratedSpoiler(String text, String provider, String model) {
    }
}
