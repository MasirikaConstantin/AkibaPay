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

        // CORRECTION : Initialiser RetrofitClient AVANT de l'utiliser
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
        // Format congolais : 09... ou +2439...
        String phoneRegex = "^(09|\\+2439)[0-9]{8}$";
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
                t.printStackTrace(); // CORRECTION : Ajouter des logs pour le débogage
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
                    handleLoginError("Erreur lors de la récupération du profil: " + errorMessage);

                    // CORRECTION : Log détaillé pour déboguer
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            System.out.println("Error body: " + errorBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                loadingHelper.hideLoading();
                handleLoginError("Erreur de connexion lors de la récupération des données.");
                t.printStackTrace();
            }
        });
    }

    // CORRECTION : Méthode saveUserData adaptée aux nouveaux types
    private void saveUserData(Users user) {
        try {
            editor.putString(KEY_USER_ID, user.getId());
            editor.putString(KEY_PHONE_NUMBER, user.getPhoneNumber());

            // CORRECTION : Sauvegarder les String au lieu des int
            editor.putString(KEY_USER_ROLE, user.getRole());
            editor.putString(KEY_USER_STATUS, user.getStatus());

            editor.putBoolean(KEY_IS_LOGGED_IN, true);

            // Sauvegarder l'objet utilisateur complet en JSON
            Gson gson = new Gson();
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER_DATA, userJson);

            // Sauvegarder un token de session
            editor.putString(KEY_SESSION_TOKEN, "session_" + System.currentTimeMillis());

            boolean success = editor.commit();

            if (success) {
                String welcomeMessage = "Bienvenue " + user.getPhoneNumber();
                if (user.getRole() != null) {
                    welcomeMessage += " (" + user.getRoleDisplayName() + ")";
                }
                Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde des données", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la sauvegarde: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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

    // CORRECTION : Méthode logout améliorée
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

    // CORRECTION : Méthode clearUserData améliorée
    private void clearUserData() {
        try {
            editor.clear();
            boolean success = editor.commit();

            if (!success) {
                // Fallback: utiliser apply() si commit() échoue
                editor.clear().apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Rediriger vers le login
        Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public static Users getLoggedInUser(SharedPreferences prefs) {
        try {
            String userJson = prefs.getString(KEY_USER_DATA, null);
            if (userJson != null) {
                Gson gson = new Gson();
                return gson.fromJson(userJson, Users.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUserRole(SharedPreferences prefs) {
        return prefs.getString(KEY_USER_ROLE, "Utilisateur");
    }

    public static String getUserStatus(SharedPreferences prefs) {
        return prefs.getString(KEY_USER_STATUS, "Inactif");
    }

    public static boolean isUserActive(SharedPreferences prefs) {
        String status = prefs.getString(KEY_USER_STATUS, "Inactif");
        return "Active".equalsIgnoreCase(status);
    }

    public static boolean isUserCaissier(SharedPreferences prefs) {
        String role = prefs.getString(KEY_USER_ROLE, "Utilisateur");
        return "Caissier".equalsIgnoreCase(role);
    }

    public static boolean isUserAdmin(SharedPreferences prefs) {
        String role = prefs.getString(KEY_USER_ROLE, "Utilisateur");
        return "Admin".equalsIgnoreCase(role) || "SuperAdmin".equalsIgnoreCase(role);
    }

    public static boolean isUserLoggedIn(SharedPreferences prefs) {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
}