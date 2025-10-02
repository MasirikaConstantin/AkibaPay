package com.example.akibapay.models;

import com.google.gson.annotations.SerializedName;

public class DailyStats {
    @SerializedName("currency")
    private String currency;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("transactionCount")
    private int transactionCount;

    // Getters et setters
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}