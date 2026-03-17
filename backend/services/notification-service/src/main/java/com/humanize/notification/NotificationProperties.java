package com.humanize.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification")
public class NotificationProperties {
    private double defaultSpoilerProgressPercent = 60.0;
    private long spoilerCooldownMinutes = 720;
    private final Email email = new Email();

    public double getDefaultSpoilerProgressPercent() {
        return defaultSpoilerProgressPercent;
    }

    public void setDefaultSpoilerProgressPercent(double defaultSpoilerProgressPercent) {
        this.defaultSpoilerProgressPercent = defaultSpoilerProgressPercent;
    }

    public long getSpoilerCooldownMinutes() {
        return spoilerCooldownMinutes;
    }

    public void setSpoilerCooldownMinutes(long spoilerCooldownMinutes) {
        this.spoilerCooldownMinutes = spoilerCooldownMinutes;
    }

    public Email getEmail() {
        return email;
    }

    public static class Email {
        private String apiKey = "";
        private String brevoBaseUrl = "https://api.brevo.com";
        private String fromEmail = "no-reply@humanize.local";
        private String fromName = "Humanize";
        private String replyToEmail = "";
        private String replyToName = "";

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBrevoBaseUrl() {
            return brevoBaseUrl;
        }

        public void setBrevoBaseUrl(String brevoBaseUrl) {
            this.brevoBaseUrl = brevoBaseUrl;
        }

        public String getFromEmail() {
            return fromEmail;
        }

        public void setFromEmail(String fromEmail) {
            this.fromEmail = fromEmail;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }

        public String getReplyToEmail() {
            return replyToEmail;
        }

        public void setReplyToEmail(String replyToEmail) {
            this.replyToEmail = replyToEmail;
        }

        public String getReplyToName() {
            return replyToName;
        }

        public void setReplyToName(String replyToName) {
            this.replyToName = replyToName;
        }
    }
}
