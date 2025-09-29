package com.example.akibapay.models;


public enum UserStatus {
    ACTIVE(0),
    INACTIVE(1),
    SUSPENDED(2);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static UserStatus fromValue(int value) {
        for (UserStatus status : UserStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return ACTIVE;
    }
}
