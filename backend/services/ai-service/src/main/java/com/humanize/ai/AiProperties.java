package com.humanize.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
    private String provider = "heuristic";
    private String model = "llama3.2:3b";
    private String ollamaBaseUrl = "http://localhost:11434";
    private int maxPreviewChars = 260;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }

    public void setOllamaBaseUrl(String ollamaBaseUrl) {
        this.ollamaBaseUrl = ollamaBaseUrl;
    }

    public int getMaxPreviewChars() {
        return maxPreviewChars;
    }

    public void setMaxPreviewChars(int maxPreviewChars) {
        this.maxPreviewChars = maxPreviewChars;
    }
}
