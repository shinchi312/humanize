package com.humanize.ingestion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MetadataHeuristicService {
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("(?i)\\bby\\s+([A-Za-z][A-Za-z .'-]{1,60})\\b");
    private static final Map<String, String> GENRE_KEYWORDS = buildGenreKeywords();

    public Metadata infer(String text) {
        String safeText = text == null ? "" : text;
        String title = inferTitle(safeText);
        String author = inferAuthor(safeText);
        String genre = inferGenre(safeText);
        return new Metadata(title, author, genre);
    }

    private String inferTitle(String text) {
        for (String line : text.split("\\R")) {
            String cleaned = line.trim();
            if (cleaned.length() >= 4 && cleaned.length() <= 100) {
                return cleaned;
            }
        }
        return "Untitled Book";
    }

    private String inferAuthor(String text) {
        Matcher matcher = AUTHOR_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Unknown Author";
    }

    private String inferGenre(String text) {
        String lower = text.toLowerCase();
        for (Map.Entry<String, String> entry : GENRE_KEYWORDS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "General";
    }

    private static Map<String, String> buildGenreKeywords() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("dragon", "Fantasy");
        map.put("magic", "Fantasy");
        map.put("spaceship", "Sci-Fi");
        map.put("galaxy", "Sci-Fi");
        map.put("detective", "Mystery");
        map.put("murder", "Mystery");
        map.put("romance", "Romance");
        map.put("kiss", "Romance");
        map.put("history", "History");
        map.put("biography", "Biography");
        return map;
    }

    public record Metadata(String title, String author, String genre) {
        public Metadata {
            if (!StringUtils.hasText(title)) {
                title = "Untitled Book";
            }
            if (!StringUtils.hasText(author)) {
                author = "Unknown Author";
            }
            if (!StringUtils.hasText(genre)) {
                genre = "General";
            }
        }
    }
}
