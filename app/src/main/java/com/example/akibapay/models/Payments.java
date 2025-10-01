package com.example.akibapay.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.UUID;

public class Payments {
    private String id;
    private Double amount;
    private String currency;
    private String status; // Déjà corrigé en String
    private String fromNumber;
    private String toNumber;

    @SerializedName("merchantReference")
    private String merchantReference;

    @SerializedName("akibaReference")
    private String akibaReference;

    @SerializedName("fromNumberTelecomReference")
    private String fromNumberTelecomReference;

    @SerializedName("toNumberTelecomReference")
    private String toNumberTelecomReference;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    // CORRECTION : Ces champs doivent être des String ou être ignorés
    @SerializedName("callbackGeneratedAt")
    private String callbackGeneratedAt;

    @SerializedName("callbackUrl")
    private String callbackUrl;

    // CORRECTION : Ces champs doivent être des String
    @SerializedName("agentUserId")
    private String agentUserId;

    @SerializedName("deviceId")
    private String deviceId;

    // Getters et setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFromNumber() {
        return fromNumber;
    }

    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }

    public String getToNumber() {
        return toNumber;
    }

    public void setToNumber(String toNumber) {
        this.toNumber = toNumber;
    }

    public String getMerchantReference() {
        return merchantReference;
    }

    public void setMerchantReference(String merchantReference) {
        this.merchantReference = merchantReference;
    }

    public String getAkibaReference() {
        return akibaReference;
    }

    public void setAkibaReference(String akibaReference) {
        this.akibaReference = akibaReference;
    }

    public String getFromNumberTelecomReference() {
        return fromNumberTelecomReference;
    }

    public void setFromNumberTelecomReference(String fromNumberTelecomReference) {
        this.fromNumberTelecomReference = fromNumberTelecomReference;
    }

    public String getToNumberTelecomReference() {
        return toNumberTelecomReference;
    }

    public void setToNumberTelecomReference(String toNumberTelecomReference) {
        this.toNumberTelecomReference = toNumberTelecomReference;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCallbackGeneratedAt() {
        return callbackGeneratedAt;
    }

    public void setCallbackGeneratedAt(String callbackGeneratedAt) {
        this.callbackGeneratedAt = callbackGeneratedAt;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getAgentUserId() {
        return agentUserId;
    }

    public void setAgentUserId(String agentUserId) {
        this.agentUserId = agentUserId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}