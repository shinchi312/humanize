package com.humanize.ingestion;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ingestion")
public class IngestionProperties {
    private String localSourceRoot = "";
    private int minExtractedChars = 400;
    private boolean ocrEnabled = true;
    private String ocrCommand = "tesseract";
    private int ocrTimeoutSeconds = 45;

    public String getLocalSourceRoot() {
        return localSourceRoot;
    }

    public void setLocalSourceRoot(String localSourceRoot) {
        this.localSourceRoot = localSourceRoot;
    }

    public int getMinExtractedChars() {
        return minExtractedChars;
    }

    public void setMinExtractedChars(int minExtractedChars) {
        this.minExtractedChars = minExtractedChars;
    }

    public boolean isOcrEnabled() {
        return ocrEnabled;
    }

    public void setOcrEnabled(boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
    }

    public String getOcrCommand() {
        return ocrCommand;
    }

    public void setOcrCommand(String ocrCommand) {
        this.ocrCommand = ocrCommand;
    }

    public int getOcrTimeoutSeconds() {
        return ocrTimeoutSeconds;
    }

    public void setOcrTimeoutSeconds(int ocrTimeoutSeconds) {
        this.ocrTimeoutSeconds = ocrTimeoutSeconds;
    }
}
