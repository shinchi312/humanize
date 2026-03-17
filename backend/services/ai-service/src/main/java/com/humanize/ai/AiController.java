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
    private final SpoilerGenerationService spoilerGenerationService;

    public AiController(SpoilerGenerationService spoilerGenerationService) {
        this.spoilerGenerationService = spoilerGenerationService;
    }

    @PostMapping("/spoiler/preview")
    public Map<String, Object> spoilerPreview(@RequestBody SpoilerRequest request) {
        SpoilerGenerationService.GeneratedSpoiler generated = spoilerGenerationService.previewFromCharacter(
                request.bookId(),
                request.characterName()
        );
        return Map.of(
                "bookId", request.bookId(),
                "preview", generated.text(),
                "provider", generated.provider(),
                "model", generated.model()
        );
    }

    public record SpoilerRequest(@NotBlank String bookId, @NotBlank String characterName) {
    }
}
