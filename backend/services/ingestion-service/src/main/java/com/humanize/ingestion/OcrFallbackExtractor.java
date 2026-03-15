package com.humanize.ingestion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OcrFallbackExtractor {
    private static final Logger log = LoggerFactory.getLogger(OcrFallbackExtractor.class);

    private final IngestionProperties ingestionProperties;

    public OcrFallbackExtractor(IngestionProperties ingestionProperties) {
        this.ingestionProperties = ingestionProperties;
    }

    public Optional<String> extract(Path pdfPath, String objectKey) {
        if (!ingestionProperties.isOcrEnabled()) {
            return Optional.empty();
        }

        ProcessBuilder processBuilder = new ProcessBuilder(
                ingestionProperties.getOcrCommand(),
                pdfPath.toString(),
                "stdout",
                "-l",
                "eng"
        );
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            boolean finished = process.waitFor(ingestionProperties.getOcrTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalArgumentException("OCR process timed out for objectKey=" + objectKey);
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (process.exitValue() != 0 || !StringUtils.hasText(output)) {
                log.warn("OCR returned no text for objectKey={} exitCode={}", objectKey, process.exitValue());
                return Optional.empty();
            }
            return Optional.of(output);
        } catch (IOException ex) {
            log.warn("OCR command unavailable or failed command={} message={}",
                    ingestionProperties.getOcrCommand(), ex.getMessage());
            return Optional.empty();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("OCR interrupted for objectKey=" + objectKey, ex);
        }
    }
}
