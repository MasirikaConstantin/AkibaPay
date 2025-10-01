package com.example.akibapay.request;

public class LoginRequest {
    private String phone;
    private String pin;

    public LoginRequest(String phone, String pin) {
        this.phone = phone;
        this.pin = pin;
    }

    // Getters et setters
    public String getphone() { return phone; }
    public void setphone(String phone) { this.phone = phone; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
}