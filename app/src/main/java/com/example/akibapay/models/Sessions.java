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
    private LocalDateTime revokedAt; // Nouveau champ pour Revoked

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

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    // Méthodes utilitaires améliorées
    public boolean isActive() {
        return status == SessionStatus.ACTIVE;
    }

    public boolean isClosed() {
        return status == SessionStatus.CLOSE;
    }

    public boolean isRevoked() {
        return status == SessionStatus.REVOKED;
    }

    public boolean isTerminated() {
        return status == SessionStatus.CLOSE || status == SessionStatus.REVOKED;
    }

    public void closeSession() {
        this.status = SessionStatus.CLOSE;
        this.closedAt = LocalDateTime.now();
    }

    public void revokeSession() {
        this.status = SessionStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }

    public void activateSession() {
        this.status = SessionStatus.ACTIVE;
        this.closedAt = null;
        this.revokedAt = null;
    }

    // Méthode pour obtenir le statut sous forme de texte affichable
    public String getStatusDisplayText() {
        switch (status) {
            case ACTIVE: return "Active";
            case CLOSE: return "Fermée";
            case REVOKED: return "Révoquée";
            default: return "Inconnu";
        }
    }

    // Méthode pour obtenir la couleur du statut (utile pour l'UI)
    public int getStatusColor() {
        switch (status) {
            case ACTIVE: return 0xFF4CAF50; // Vert
            case CLOSE: return 0xFFFF9800; // Orange
            case REVOKED: return 0xFFF44336; // Rouge
            default: return 0xFF9E9E9E; // Gris
        }
    }
}