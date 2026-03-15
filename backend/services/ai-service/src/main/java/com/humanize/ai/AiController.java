package com.humanize.ai;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Validated
public class AiController {

    @PostMapping("/spoiler/preview")
    public Map<String, Object> spoilerPreview(@RequestBody SpoilerRequest request) {
        String preview = "You are three chapters away from a major reveal involving " + request.characterName() + ".";
        return Map.of("bookId", request.bookId(), "preview", preview, "model", "gemini-placeholder");
    }

    public record SpoilerRequest(@NotBlank String bookId, @NotBlank String characterName) {
    }
}
