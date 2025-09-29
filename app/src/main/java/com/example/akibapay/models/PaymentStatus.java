package com.example.akibapay.models;

public enum PaymentStatus {
    PENDING(0),
    COMPLETED(1),
    FAILED(2);

    private final int value;

    PaymentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PaymentStatus fromValue(int value) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return PENDING;
    }
}