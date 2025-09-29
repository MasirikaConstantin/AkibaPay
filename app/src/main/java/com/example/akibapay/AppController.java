package com.example.akibapay;

import android.app.Application;

import com.example.akibapay.api.RetrofitClient;

public class AppController extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialiser RetrofitClient avec le contexte application
        RetrofitClient.initialize(this);
    }
}