package com.humanize.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreferenceEntity {
    @Id
    @Column(name = "user_id", nullable = false, length = 128)
    private String userId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "email_address", length = 255)
    private String emailAddress;

    @Column(name = "spoiler_enabled", nullable = false)
    private boolean spoilerEnabled;

    @Column(name = "spoiler_min_progress_percent", nullable = false)
    private double spoilerMinProgressPercent;

    @Column(name = "preferred_channel", nullable = false, length = 32)
    private String preferredChannel;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public boolean isSpoilerEnabled() {
        return spoilerEnabled;
    }

    public void setSpoilerEnabled(boolean spoilerEnabled) {
        this.spoilerEnabled = spoilerEnabled;
    }

    public double getSpoilerMinProgressPercent() {
        return spoilerMinProgressPercent;
    }

    public void setSpoilerMinProgressPercent(double spoilerMinProgressPercent) {
        this.spoilerMinProgressPercent = spoilerMinProgressPercent;
    }

    public String getPreferredChannel() {
        return preferredChannel;
    }

    public void setPreferredChannel(String preferredChannel) {
        this.preferredChannel = preferredChannel;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
