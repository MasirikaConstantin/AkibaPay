package com.example.akibapay.request;


public class UpdatePasswordRequest {
    private String newPinHash;

    public UpdatePasswordRequest() {}

    public UpdatePasswordRequest(String newPinHash) {
        this.newPinHash = newPinHash;
    }

    // Getters and Setters
    public String getNewPinHash() { return newPinHash; }
    public void setNewPinHash(String newPinHash) { this.newPinHash = newPinHash; }
}