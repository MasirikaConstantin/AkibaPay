package com.example.akibapay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.akibapay.api.RetrofitClient;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String PREFS_NAME = "AkibaPayPrefs";
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "SplashActivity créée");

        // Initialisations immédiates
        initializeApp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Vérification immédiate après que l'activity soit visible
        checkAuthentication();
    }

    private void initializeApp() {
        try {
            RetrofitClient.initialize(this);
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            Log.d(TAG, "Initialisation terminée");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation", e);
            navigateToLogin();
        }
    }

    private void checkAuthentication() {
        new Thread(() -> {
            try {
                // Simuler un temps de traitement minimal
                Thread.sleep(500); // Juste pour laisser le splash s'afficher

                runOnUiThread(() -> {
                    boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
                    Log.d(TAG, "Utilisateur connecté: " + isLoggedIn);

                    if (isLoggedIn && isUserDataValid()) {
                        navigateToHome();
                    } else {
                        navigateToLogin();
                    }
                });
            } catch (InterruptedException e) {
                runOnUiThread(this::navigateToLogin);
            }
        }).start();
    }

    private boolean isUserDataValid() {
        try {
            String userId = sharedPreferences.getString("user_id", "");
            String phone = sharedPreferences.getString("phone_number", "");
            boolean isValid = !userId.isEmpty() && !phone.isEmpty();
            Log.d(TAG, "Données utilisateur valides: " + isValid);
            return isValid;
        } catch (Exception e) {
            Log.e(TAG, "Erreur vérification données", e);
            return false;
        }
    }

    private void navigateToHome() {
        Log.d(TAG, "Navigation vers HomeActivity");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Log.d(TAG, "Navigation vers LoginActivity");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity détruite");
    }
}