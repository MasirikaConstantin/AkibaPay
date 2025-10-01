package com.example.akibapay.models;

public class Users {
    private String id;
    private String phoneNumber;
    private String pinHash;
    private String role; // CORRECTION : Changé de int à String
    private String status; // CORRECTION : Changé de int à String
    private String createdAt;
    private String updatedAt;

    // Getters et setters pour tous les champs
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public String getRole() { return role; } // CORRECTION : String
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; } // CORRECTION : String
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Méthodes utilitaires pour faciliter l'utilisation
    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    public boolean isCaissier() {
        return "Caissier".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    public String getRoleDisplayName() {
        if (role == null) return "Utilisateur";
        switch (role.toLowerCase()) {
            case "caissier": return "Caissier";
            case "admin": return "Administrateur";
            case "superadmin": return "Super Administrateur";
            default: return role;
        }
    }
}