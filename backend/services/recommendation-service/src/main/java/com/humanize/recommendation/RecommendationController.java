package com.humanize.recommendation;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationScoringService scoringService;

    public RecommendationController(RecommendationScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @GetMapping("/{userId}")
    public Map<String, Object> getRecommendations(@PathVariable String userId) {
        List<Map<String, Object>> recommendations = scoringService.topRecommendations(userId);
        if (recommendations.isEmpty()) {
            recommendations = List.of(
                    Map.of("bookId", "book-101", "score", 0.0, "reason", "cold_start", "title", "Unknown", "author", "Unknown", "genre", "General"),
                    Map.of("bookId", "book-205", "score", 0.0, "reason", "cold_start", "title", "Unknown", "author", "Unknown", "genre", "General"),
                    Map.of("bookId", "book-377", "score", 0.0, "reason", "cold_start", "title", "Unknown", "author", "Unknown", "genre", "General")
            );
        }

        return Map.of(
                "userId", userId,
                "scope", "private-library",
                "recommendations", recommendations
        );
    }
}
