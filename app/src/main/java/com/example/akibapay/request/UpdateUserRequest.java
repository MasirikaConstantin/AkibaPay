package com.example.akibapay.request;

public class UpdateUserRequest {
    private String type;
    private int value;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String type, int value) {
        this.type = type;
        this.value = value;
    }

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}