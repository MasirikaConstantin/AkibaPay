package com.example.akibapay.models;

public enum SessionStatus {
    ACTIVE(0),
    EXPIRED(1),
    CLOSED(2);

    private final int value;

    SessionStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SessionStatus fromValue(int value) {
        for (SessionStatus status : SessionStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return ACTIVE;
    }
}