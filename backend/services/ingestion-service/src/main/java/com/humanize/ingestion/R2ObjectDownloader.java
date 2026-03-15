package com.humanize.ingestion;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Component
public class R2ObjectDownloader {
    private final IngestionR2Properties r2Properties;

    public R2ObjectDownloader(IngestionR2Properties r2Properties) {
        this.r2Properties = r2Properties;
    }

    public Path downloadToTempFile(String objectKey) {
        validateConfig();
        try {
            Path tempFile = Files.createTempFile("humanize-ingest-", ".pdf");
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(r2Properties.getBucket())
                    .key(objectKey)
                    .build();

            try (S3Client s3Client = createClient()) {
                s3Client.getObject(request, ResponseTransformer.toFile(tempFile));
            }
            return tempFile;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to download object from R2 key=" + objectKey, ex);
        }
    }

    private S3Client createClient() {
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2Properties.getAccessKeyId(), r2Properties.getSecretAccessKey())
                ))
                .endpointOverride(URI.create(r2Properties.effectiveEndpoint()))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private void validateConfig() {
        if (!StringUtils.hasText(r2Properties.getAccessKeyId()) || !StringUtils.hasText(r2Properties.getSecretAccessKey())) {
            throw new IllegalStateException("R2 credentials missing. Set R2_ACCESS_KEY_ID and R2_SECRET_ACCESS_KEY.");
        }
        if (!StringUtils.hasText(r2Properties.getBucket())) {
            throw new IllegalStateException("R2 bucket missing. Set R2_BUCKET.");
        }
    }
}
