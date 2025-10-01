package com.example.akibapay.models;

public enum SessionStatus {
    ACTIVE("Active"),
    CLOSE("Close"),
    REVOKED("Revoked");

    private final String value;

    SessionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SessionStatus fromValue(String value) {
        if (value == null) {
            return ACTIVE;
        }

        for (SessionStatus status : SessionStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        // Fallback pour la rétrocompatibilité si nécessaire
        return ACTIVE;
    }

    // Méthode utilitaire pour la conversion depuis l'ancien format numérique
    public static SessionStatus fromLegacyValue(int value) {
        switch (value) {
            case 0: return ACTIVE;
            case 1: return CLOSE; // EXPIRED devient CLOSE
            case 2: return REVOKED; // CLOSED devient REVOKED
            default: return ACTIVE;
        }
    }
}