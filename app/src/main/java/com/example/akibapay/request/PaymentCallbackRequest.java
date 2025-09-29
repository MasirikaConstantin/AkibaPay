package com.example.akibapay.request;

import java.time.LocalDateTime;

public class PaymentCallbackRequest {
    private String akibaReference;
    private String merchantReference;
    private String transactionStatus;
    private String toNumberTelecomReference;
    private String fromNumberTelecomReference;
    private double amount;
    private String currency;
    private String toNumber;
    private String fromNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime callbackGeneratedAt;

    public PaymentCallbackRequest() {}

    // Getters and Setters
    public String getAkibaReference() { return akibaReference; }
    public void setAkibaReference(String akibaReference) { this.akibaReference = akibaReference; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }

    public String getTransactionStatus() { return transactionStatus; }
    public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }

    public String getToNumberTelecomReference() { return toNumberTelecomReference; }
    public void setToNumberTelecomReference(String toNumberTelecomReference) { this.toNumberTelecomReference = toNumberTelecomReference; }

    public String getFromNumberTelecomReference() { return fromNumberTelecomReference; }
    public void setFromNumberTelecomReference(String fromNumberTelecomReference) { this.fromNumberTelecomReference = fromNumberTelecomReference; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getToNumber() { return toNumber; }
    public void setToNumber(String toNumber) { this.toNumber = toNumber; }

    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getCallbackGeneratedAt() { return callbackGeneratedAt; }
    public void setCallbackGeneratedAt(LocalDateTime callbackGeneratedAt) { this.callbackGeneratedAt = callbackGeneratedAt; }
}
