package com.example.akibapay.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.models.Users;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrefsManager {
    private static final String PREFS_NAME = "AkibaPayPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_STATUS = "user_status";
    private static final String KEY_USER_DATA = "user_data";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SESSION_TOKEN = "session_token";

    private static PrefsManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    private PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    public static synchronized PrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new PrefsManager(context.getApplicationContext());
        }
        return instance;
    }

    // ==================== INTERFACE CALLBACK ====================

    public interface LogoutCallback {
        void onLogoutComplete(boolean success);
    }

    // ==================== MÉTHODES DE CONNEXION ====================

    public void saveUserData(Users user) {
        editor.putString(KEY_USER_ID, user.getId().toString());
        editor.putString(KEY_PHONE_NUMBER, user.getPhoneNumber());
        editor.putInt(KEY_USER_ROLE, user.getRole());
        editor.putInt(KEY_USER_STATUS, user.getStatus());
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // Sauvegarder l'objet complet
        String userJson = gson.toJson(user);
        editor.putString(KEY_USER_DATA, userJson);

        // Générer un token de session
        editor.putString(KEY_SESSION_TOKEN, "session_" + System.currentTimeMillis());

        editor.apply();
    }

    public void clearUserData() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_PHONE_NUMBER);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_USER_STATUS);
        editor.remove(KEY_USER_DATA);
        editor.remove(KEY_IS_LOGGED_IN);
        editor.remove(KEY_SESSION_TOKEN);
        editor.apply();
    }

    // ==================== MÉTHODE DE DÉCONNEXION ====================

    public void logoutUser(Context context, LogoutCallback callback) {
        String userId = getUserId();

        // Déconnexion locale immédiate
        clearUserData();

        // Si on a un userId, on appelle l'API de déconnexion
        if (!userId.isEmpty()) {
            callLogoutApi(context, userId, callback);
        } else {
            // Pas d'userId, on termine directement
            if (callback != null) {
                callback.onLogoutComplete(true);
            }
        }
    }

    private void callLogoutApi(Context context, String userId, LogoutCallback callback) {
        try {
            ApiService apiService = RetrofitClient.getApiService();
            Call<Void> call = apiService.logout(userId);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    // L'API a répondu, peu importe le statut on considère la déconnexion comme réussie
                    boolean apiSuccess = response.isSuccessful();

                    if (callback != null) {
                        callback.onLogoutComplete(true);
                    }

                    // Log pour débogage
                    android.util.Log.d("PrefsManager", "Déconnexion API: " + (apiSuccess ? "SUCCESS" : "FAILED but local logout done"));
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Erreur réseau, mais la déconnexion locale est déjà faite
                    if (callback != null) {
                        callback.onLogoutComplete(true);
                    }

                    android.util.Log.e("PrefsManager", "Erreur réseau lors de la déconnexion API", t);
                }
            });
        } catch (Exception e) {
            // Erreur lors de l'appel API, mais déconnexion locale déjà faite
            if (callback != null) {
                callback.onLogoutComplete(true);
            }

            android.util.Log.e("PrefsManager", "Exception lors de la déconnexion API", e);
        }
    }

    // ==================== MÉTHODES DE VÉRIFICATION ====================

    public boolean isUserLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isUserDataValid() {
        return !getUserId().isEmpty() &&
                !getPhoneNumber().isEmpty() &&
                getLoggedInUser() != null;
    }

    // ==================== GETTERS SIMPLES ====================

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getPhoneNumber() {
        return prefs.getString(KEY_PHONE_NUMBER, "");
    }

    public int getUserRole() {
        return prefs.getInt(KEY_USER_ROLE, 0);
    }

    public int getUserStatus() {
        return prefs.getInt(KEY_USER_STATUS, 0);
    }

    public String getSessionToken() {
        return prefs.getString(KEY_SESSION_TOKEN, "");
    }

    public boolean getIsLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // ==================== GETTERS AVEC OBJETS ====================

    public Users getLoggedInUser() {
        try {
            String userJson = prefs.getString(KEY_USER_DATA, null);
            if (userJson != null) {
                return gson.fromJson(userJson, Users.class);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== MÉTHODES DE MISES À JOUR INDIVIDUELLES ====================

    public void updatePhoneNumber(String phoneNumber) {
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);

        // Mettre à jour aussi dans l'objet user complet
        Users user = getLoggedInUser();
        if (user != null) {
            user.setPhoneNumber(phoneNumber);
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER_DATA, userJson);
        }

        editor.apply();
    }

    public void updateUserRole(int role) {
        editor.putInt(KEY_USER_ROLE, role);

        Users user = getLoggedInUser();
        if (user != null) {
            // Note: Vous devrez adapter selon votre enum UserRole
            // user.setRole(UserRole.fromValue(role));
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER_DATA, userJson);
        }

        editor.apply();
    }

    public void updateUserStatus(int status) {
        editor.putInt(KEY_USER_STATUS, status);

        Users user = getLoggedInUser();
        if (user != null) {
            // Note: Vous devrez adapter selon votre enum UserStatus
            // user.setStatus(UserStatus.fromValue(status));
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER_DATA, userJson);
        }

        editor.apply();
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    public void refreshSessionToken() {
        editor.putString(KEY_SESSION_TOKEN, "session_" + System.currentTimeMillis());
        editor.apply();
    }

    public boolean hasValidSession() {
        String token = getSessionToken();
        return !token.isEmpty() && isUserLoggedIn();
    }

    // ==================== DEBUG ET MAINTENANCE ====================

    public void printAllPreferences() {
        android.util.Log.d("PrefsManager", "=== DÉBUT PREFS ===");
        android.util.Log.d("PrefsManager", "User ID: " + getUserId());
        android.util.Log.d("PrefsManager", "Phone: " + getPhoneNumber());
        android.util.Log.d("PrefsManager", "Role: " + getUserRole());
        android.util.Log.d("PrefsManager", "Status: " + getUserStatus());
        android.util.Log.d("PrefsManager", "Logged In: " + isUserLoggedIn());
        android.util.Log.d("PrefsManager", "Session Token: " + getSessionToken());
        android.util.Log.d("PrefsManager", "=== FIN PREFS ===");
    }

    public void clearAllPreferences() {
        editor.clear().apply();
    }
}