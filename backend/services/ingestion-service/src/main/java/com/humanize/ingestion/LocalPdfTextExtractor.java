package com.humanize.ingestion;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LocalPdfTextExtractor {
    public Optional<String> extract(Path pdfPath) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (!StringUtils.hasText(text)) {
                return Optional.empty();
            }
            return Optional.of(text.trim());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Failed reading PDF at " + pdfPath, ex);
        }
    }
}
