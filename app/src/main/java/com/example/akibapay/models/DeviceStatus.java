package com.example.akibapay.models;


public enum DeviceStatus {
    UNKNOWN(0),
    ACTIVE(1),
    INACTIVE(2),
    MAINTENANCE(3);

    private final int value;

    DeviceStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DeviceStatus fromValue(int value) {
        for (DeviceStatus status : DeviceStatus.values()) {
            if (status.value == value) {
                return status;
            }
        }
        return UNKNOWN;
    }
}