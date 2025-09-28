package com.example.akibapay.request;


public class LoginRequest {
    private String phone;
    private String pin;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String phone, String pin) {
        this.phone = phone;
        this.pin = pin;
    }

    // Getters and Setters
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}