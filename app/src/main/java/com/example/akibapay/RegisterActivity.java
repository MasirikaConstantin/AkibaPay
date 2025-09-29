package com.example.akibapay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.helper.LoadingDialogHelper;
import com.example.akibapay.models.Users;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    private EditText  etConfirmPin;
    private TextInputEditText etPhone, etPin;
    private LoadingDialogHelper loadingHelper;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String PREFS_NAME = "AkibaPayPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_STATUS = "user_status";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        loadingHelper = new LoadingDialogHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initView();
        checkIfAlreadyLoggedIn();
    }

    private void initView(){
        btnRegister = findViewById(R.id.btnRegister);
        etPhone = findViewById(R.id.phoneInput);
        etPin = findViewById(R.id.pinInput);
        etConfirmPin = findViewById(R.id.etConfirmPin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });
    }

    private void attemptRegistration() {
        String phone = etPhone.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String confirmPin = etConfirmPin.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Le numéro de téléphone est requis");
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pin)) {
            etPin.setError("Le PIN est requis");
            etPin.requestFocus();
            return;
        }

        if (pin.length() != 4) {
            etPin.setError("Le PIN doit contenir 4 chiffres");
            etPin.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPin)) {
            etConfirmPin.setError("Veuillez confirmer le PIN");
            etConfirmPin.requestFocus();
            return;
        }

        if (!pin.equals(confirmPin)) {
            etConfirmPin.setError("Les PIN ne correspondent pas");
            etConfirmPin.requestFocus();
            return;
        }

        // Valider le format du numéro de téléphone
        if (!isValidPhoneNumber(phone)) {
            etPhone.setError("Numéro de téléphone invalide");
            etPhone.requestFocus();
            return;
        }

        registerUser(phone, pin);
    }

    private boolean isValidPhoneNumber(String phone) {
        // Validation basique du numéro de téléphone
        String phoneRegex = "^[+]?[0-9]{9,13}$";
        return phone.matches(phoneRegex);
    }

    private void registerUser(String phone, String pin) {
        loadingHelper.showLoading("Création de votre compte...");

        // Créer la requête d'inscription
        Users userRequest = new Users();
        userRequest.setPhoneNumber(phone);
        userRequest.setPinHash(hashPin(pin)); // Dans une app réelle, hash le PIN
        // Les autres champs seront définis par le serveur (role, status, etc.)

        ApiService apiService = RetrofitClient.getApiService();
        Call<Users> call = apiService.createUser(userRequest);

        call.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                loadingHelper.hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    Users user = response.body();
                    //saveUserData(user);
                    showSuccessDialog();
                } else {
                    String errorMessage = getErrorMessage(response);
                    handleRegistrationError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                loadingHelper.hideLoading();
                handleRegistrationError("Erreur de connexion. Vérifiez votre internet.");
            }
        });


    }
    private String getErrorMessage(Response<Users> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();

                // Parser le JSON pour extraire le message
                if (errorBody.contains("message")) {
                    JsonObject jsonObject = new Gson().fromJson(errorBody, JsonObject.class);
                    if (jsonObject.has("message")) {
                        String serverMessage = jsonObject.get("message").getAsString();

                        // Traduire les messages connus
                        if ("User with the same phone number already exists.".equals(serverMessage)) {
                            return "Un utilisateur avec ce numéro de téléphone existe déjà.";
                        }
                        return serverMessage; // Retourner le message original si non traduit
                    }
                }
                return errorBody; // Retourner le body complet si structure inconnue
            }
        } catch (Exception e) {
            Log.e("API_ERROR", "Error parsing error message", e);
        }

        // Messages par défaut selon le code HTTP
        switch (response.code()) {
            case 409:
                return "Un utilisateur avec ce numéro de téléphone existe déjà.";
            case 400:
                return "Données invalides. Vérifiez les informations saisies.";
            case 500:
                return "Erreur interne du serveur. Veuillez réessayer plus tard.";
            default:
                return "Échec de l'inscription. Code d'erreur: " + response.code();
        }
    }

    private String hashPin(String pin) {
        // Dans une application réelle, utilisez un hash sécurisé comme BCrypt
        // Pour le moment, on retourne le PIN tel quel (à améliorer)
        return pin;
    }

   /* private void saveUserData(Users user) {
        // Sauvegarder les données utilisateur dans SharedPreferences
        editor.putString(KEY_USER_ID, user.getId().toString());
        editor.putString(KEY_PHONE_NUMBER, user.getPhoneNumber());
        editor.putInt(KEY_USER_ROLE, user.getRole());
        editor.putInt(KEY_USER_STATUS, user.getStatus());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // Sauvegarder l'objet utilisateur complet en JSON
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER_DATA, userJson);

        editor.apply();
    }*/

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        Button btnContinue = dialogView.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            navigateToMainActivity();
        });

        dialog.show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleRegistrationError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Erreur")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void checkIfAlreadyLoggedIn() {
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            //navigateToMainActivity();
        }
    }

    // Méthode utilitaire pour récupérer l'utilisateur connecté
    public static Users getLoggedInUser(SharedPreferences prefs) {
        String userJson = prefs.getString(KEY_USER_DATA, null);
        if (userJson != null) {
            Gson gson = new Gson();
            return gson.fromJson(userJson, Users.class);
        }
        return null;
    }

    // Méthode utilitaire pour vérifier si un utilisateur est connecté
    public static boolean isUserLoggedIn(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Méthode utilitaire pour déconnecter l'utilisateur
    public static void logoutUser(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_PHONE_NUMBER);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_USER_STATUS);
        editor.remove(KEY_USER_DATA);
        editor.remove(KEY_IS_LOGGED_IN);
        editor.apply();
    }
}