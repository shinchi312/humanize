package com.humanize.ingestion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PdfSourceResolver {
    private final IngestionProperties ingestionProperties;
    private final R2ObjectDownloader r2ObjectDownloader;

    public PdfSourceResolver(IngestionProperties ingestionProperties, R2ObjectDownloader r2ObjectDownloader) {
        this.ingestionProperties = ingestionProperties;
        this.r2ObjectDownloader = r2ObjectDownloader;
    }

    public ResolvedPdfSource resolve(String objectKey) {
        if (StringUtils.hasText(ingestionProperties.getLocalSourceRoot())) {
            Path localPath = Paths.get(ingestionProperties.getLocalSourceRoot(), objectKey);
            if (Files.exists(localPath)) {
                return new ResolvedPdfSource(localPath, "LOCAL_MIRROR", false);
            }
        }

        Path downloadedPath = r2ObjectDownloader.downloadToTempFile(objectKey);
        return new ResolvedPdfSource(downloadedPath, "R2_DOWNLOAD", true);
    }
}
