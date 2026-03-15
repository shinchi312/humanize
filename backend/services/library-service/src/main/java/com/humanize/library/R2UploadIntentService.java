package com.humanize.library;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Presigner;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class R2UploadIntentService {
    private static final Pattern UNSAFE_KEY_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");

    private final R2Properties r2Properties;

    public R2UploadIntentService(R2Properties r2Properties) {
        this.r2Properties = r2Properties;
    }

    public UploadIntent createIntent(UploadIntentInput input) {
        validateConfig();

        String objectKey = "users/%s/books/%s/%d-%s".formatted(
                safe(input.userId()),
                safe(input.bookId()),
                Instant.now().toEpochMilli(),
                safe(input.fileName())
        );

        Duration ttl = Duration.ofMinutes(r2Properties.getUploadUrlTtlMinutes());
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.getBucket())
                .key(objectKey)
                .contentType(input.contentType())
                .build();

        try (S3Presigner presigner = createPresigner()) {
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
            return new UploadIntent(
                    objectKey,
                    presigned.url().toString(),
                    Map.of("Content-Type", input.contentType()),
                    Instant.now().plus(ttl).toString()
            );
        }
    }

    private S3Presigner createPresigner() {
        return S3Presigner.builder()
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
            throw new IllegalStateException("R2 credentials are missing. Set R2_ACCESS_KEY_ID and R2_SECRET_ACCESS_KEY.");
        }
        if (!StringUtils.hasText(r2Properties.getBucket())) {
            throw new IllegalStateException("R2 bucket is missing. Set R2_BUCKET.");
        }
    }

    private static String safe(String value) {
        if (!StringUtils.hasText(value)) {
            return "unknown";
        }
        return UNSAFE_KEY_CHARS.matcher(value).replaceAll("-");
    }

    public record UploadIntentInput(
            String userId,
            String bookId,
            String fileName,
            String contentType
    ) {
    }

    public record UploadIntent(
            String objectKey,
            String uploadUrl,
            Map<String, String> requiredHeaders,
            String expiresAt
    ) {
    }
}
