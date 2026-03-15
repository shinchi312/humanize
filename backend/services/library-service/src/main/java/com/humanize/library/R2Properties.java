package com.humanize.library;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.r2")
public class R2Properties {
    private String accountId = "";
    private String endpoint = "";
    private String bucket = "humanize-books";
    private String accessKeyId = "";
    private String secretAccessKey = "";
    private int uploadUrlTtlMinutes = 15;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public int getUploadUrlTtlMinutes() {
        return uploadUrlTtlMinutes;
    }

    public void setUploadUrlTtlMinutes(int uploadUrlTtlMinutes) {
        this.uploadUrlTtlMinutes = uploadUrlTtlMinutes;
    }

    public String effectiveEndpoint() {
        if (StringUtils.hasText(endpoint)) {
            return endpoint;
        }
        if (!StringUtils.hasText(accountId)) {
            throw new IllegalStateException("R2 endpoint is missing. Set R2_ENDPOINT or R2_ACCOUNT_ID.");
        }
        return "https://" + accountId + ".r2.cloudflarestorage.com";
    }
}
