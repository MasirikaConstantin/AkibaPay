package com.example.akibapay.models;

import java.util.UUID;

public class Devices {
    private UUID id;
    private String deviceId;
    private String model;
    private DeviceStatus status;
    private UUID boundUserId;

    // Constructeurs
    public Devices() {}

    public Devices(UUID id, String deviceId, String model, DeviceStatus status, UUID boundUserId) {
        this.id = id;
        this.deviceId = deviceId;
        this.model = model;
        this.status = status;
        this.boundUserId = boundUserId;
    }

    // Getters et Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public DeviceStatus getStatus() { return status; }
    public void setStatus(DeviceStatus status) { this.status = status; }

    public UUID getBoundUserId() { return boundUserId; }
    public void setBoundUserId(UUID boundUserId) { this.boundUserId = boundUserId; }

    // Méthode utilitaire pour le statut
    public boolean isAvailable() {
        return status != null && status.getValue() == 1; // ACTIVE
    }
}