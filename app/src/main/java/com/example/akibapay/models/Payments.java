package com.example.akibapay.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Payments {
    private UUID id;
    private String akibaReference;
    private String merchantReference;
    private double amount;
    private String currency;
    private String fromNumber;
    private String toNumber;
    private String fromNumberTelecomReference;
    private String toNumberTelecomReference;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime callbackGeneratedAt;
    private UUID agentUserId;
    private UUID deviceId;

    public Payments() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getAkibaReference() { return akibaReference; }
    public void setAkibaReference(String akibaReference) { this.akibaReference = akibaReference; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }

    public String getToNumber() { return toNumber; }
    public void setToNumber(String toNumber) { this.toNumber = toNumber; }

    public String getFromNumberTelecomReference() { return fromNumberTelecomReference; }
    public void setFromNumberTelecomReference(String fromNumberTelecomReference) { this.fromNumberTelecomReference = fromNumberTelecomReference; }

    public String getToNumberTelecomReference() { return toNumberTelecomReference; }
    public void setToNumberTelecomReference(String toNumberTelecomReference) { this.toNumberTelecomReference = toNumberTelecomReference; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCallbackGeneratedAt() { return callbackGeneratedAt; }
    public void setCallbackGeneratedAt(LocalDateTime callbackGeneratedAt) { this.callbackGeneratedAt = callbackGeneratedAt; }

    public UUID getAgentUserId() { return agentUserId; }
    public void setAgentUserId(UUID agentUserId) { this.agentUserId = agentUserId; }

    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }

    // Méthodes utilitaires
    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }
}