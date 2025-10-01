package com.example.akibapay.models;

public enum UserRole {
    CAISSIER("Caissier"),
    ADMIN("Admin"),
    SUPER_ADMIN("SuperAdmin");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserRole fromValue(String value) {
        if (value == null) return CAISSIER;

        for (UserRole role : UserRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return CAISSIER;
    }

    public String getDisplayName() {
        switch (this) {
            case CAISSIER: return "Caissier";
            case ADMIN: return "Administrateur";
            case SUPER_ADMIN: return "Super Administrateur";
            default: return "Utilisateur";
        }
    }
}