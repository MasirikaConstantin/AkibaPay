package com.example.akibapay.request;

public class PaymentRequest {
    private String merchantId;
    private String merchantPass;
    private String fromNumber;
    private String toNumber;
    private String hash;
    private String userId;
    private double amount;
    private String currency;
    private String callbackUrl;
    private String merchantReference;

    public PaymentRequest() {}

    public PaymentRequest(String merchantId, String merchantPass, String fromNumber,
                          String toNumber, String hash, double amount, String currency,
                          String callbackUrl, String merchantReference, String userId) {
        this.merchantId = merchantId;
        this.merchantPass = merchantPass;
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.hash = hash;
        this.amount = amount;
        this.currency = currency;
        this.userId = userId;
        this.callbackUrl = callbackUrl;
        this.merchantReference = merchantReference;
    }

    // Getters and Setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getMerchantPass() { return merchantPass; }
    public void setMerchantPass(String merchantPass) { this.merchantPass = merchantPass; }

    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }

    public String getToNumber() { return toNumber; }
    public void setToNumber(String toNumber) { this.toNumber = toNumber; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }

    public String getMerchantReference() { return merchantReference; }
    public void setMerchantReference(String merchantReference) { this.merchantReference = merchantReference; }
}