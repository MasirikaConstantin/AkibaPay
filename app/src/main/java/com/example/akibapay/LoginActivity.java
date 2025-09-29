package com.example.akibapay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.helper.LoadingDialogHelper;
import com.example.akibapay.models.Users;
import com.example.akibapay.request.LoginRequest;
import com.example.akibapay.utils.ErrorHandler;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etPhone, etPin;
    private Button btnLogin;
    private TextView tvRegister;
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
    private static final String KEY_SESSION_TOKEN = "session_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        RetrofitClient.initialize(this);

        loadingHelper = new LoadingDialogHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        initView();
        checkIfAlreadyLoggedIn();
    }

    private void initView() {
        etPhone = findViewById(R.id.etPhone);
        etPin = findViewById(R.id.etPin);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegister();
            }
        });
    }

    private void attemptLogin() {
        String phone = etPhone.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

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

        if (!isValidPhoneNumber(phone)) {
            etPhone.setError("Numéro de téléphone invalide");
            etPhone.requestFocus();
            return;
        }

        loginUser(phone, pin);
    }

    private boolean isValidPhoneNumber(String phone) {
        // Autorise :
        // - optionnellement un + au début
        // - entre 9 et 13 chiffres ensuite
        String phoneRegex = "^[+]?[0-9]{9,13}$";
        return phone != null && phone.matches(phoneRegex);
    }


    private void loginUser(String phone, String pin) {
        loadingHelper.showLoading("Connexion en cours...");

        LoginRequest loginRequest = new LoginRequest(phone, pin);

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.login(loginRequest);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Login réussi, maintenant récupérer les infos utilisateur
                    fetchUserInfo(phone);
                } else {
                    loadingHelper.hideLoading();
                    String errorMessage = ErrorHandler.getErrorMessage(response);
                    handleLoginError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loadingHelper.hideLoading();
                handleLoginError("Erreur de connexion. Vérifiez votre internet.");
            }
        });
    }

    private void fetchUserInfo(String phone) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<Users> call = apiService.getUserByPhone(phone);

        call.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                loadingHelper.hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    Users user = response.body();
                    saveUserData(user);
                    navigateToMainActivity();
                } else {
                    String errorMessage = ErrorHandler.getErrorMessage(response);
                    handleLoginError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                loadingHelper.hideLoading();
                handleLoginError("Erreur de connexion lors de la récupération des données.");
            }
        });
    }

    private void saveUserData(Users user) {
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

        // Sauvegarder un token de session (simulé pour l'exemple)
        editor.putString(KEY_SESSION_TOKEN, "session_" + System.currentTimeMillis());

        editor.apply();

        // Log pour débogage
        Toast.makeText(this, "Bienvenue " + user.getPhoneNumber() + "!", Toast.LENGTH_SHORT).show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void handleLoginError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void checkIfAlreadyLoggedIn() {
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            navigateToMainActivity();
        }
    }

    // Méthode pour gérer la déconnexion
    public void logout() {
        String userId = sharedPreferences.getString(KEY_USER_ID, null);
        if (userId != null) {
            // Appeler l'API de logout
            ApiService apiService = RetrofitClient.getApiService();
            Call<Void> call = apiService.logout(userId);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // Même si l'API échoue, on déconnecte localement
                    clearUserData();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // En cas d'erreur réseau, on déconnecte quand même localement
                    clearUserData();
                }
            });
        } else {
            clearUserData();
        }
    }

    private void clearUserData() {
        RegisterActivity.logoutUser(sharedPreferences);

        // Rediriger vers le login
        Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Méthode utilitaire pour récupérer l'utilisateur connecté
    public static Users getLoggedInUser(SharedPreferences prefs) {
        return RegisterActivity.getLoggedInUser(prefs);
    }

    // Méthode utilitaire pour vérifier si un utilisateur est connecté
    public static boolean isUserLoggedIn(SharedPreferences prefs) {
        return RegisterActivity.isUserLoggedIn(prefs);
    }
}