package com.example.akibapay.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Sessions {
    private UUID id;
    private UUID userId;
    private UUID deviceId;
    private String jwtId;
    private LocalDateTime issuedAt;
    private SessionStatus status;
    private LocalDateTime closedAt;

    public Sessions() {}

    public Sessions(UUID userId, UUID deviceId, String jwtId, SessionStatus status) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.jwtId = jwtId;
        this.status = status;
        this.issuedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    public String getJwtId() { return jwtId; }
    public void setJwtId(String jwtId) { this.jwtId = jwtId; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }

    // Méthodes utilitaires
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    public boolean isExpired() {
        return status == SessionStatus.EXPIRED;
    }

    public boolean isClosed() {
        return status == SessionStatus.CLOSED;
    }

    public void closeSession() {
        this.status = SessionStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }
}