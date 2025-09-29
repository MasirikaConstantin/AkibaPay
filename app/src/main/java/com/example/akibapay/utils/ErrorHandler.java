package com.example.akibapay.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Response;

public class ErrorHandler {

    public static String getErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.d("ERROR_HANDLER", "Raw error body: " + errorBody);

                return parseErrorMessage(errorBody, response.code());
            }
        } catch (IOException e) {
            Log.e("ERROR_HANDLER", "Error reading error body", e);
        }

        return getDefaultErrorMessage(response.code());
    }

    private static String parseErrorMessage(String errorBody, int httpCode) {
        // Map des messages serveur -> français
        Map<String, String> errorMessages = new HashMap<>();

        // Erreurs d'inscription
        errorMessages.put("User with the same phone number already exists.",
                "Un utilisateur avec ce numéro de téléphone existe déjà.");
        errorMessages.put("Phone number is required.",
                "Le numéro de téléphone est requis.");
        errorMessages.put("PIN is required.",
                "Le PIN est requis.");
        errorMessages.put("Phone and PIN are required.",
                "Le numéro de téléphone et le PIN sont requis.");

        // Erreurs de connexion
        errorMessages.put("Invalid phone or PIN.",
                "Numéro de téléphone ou PIN incorrect.");
        errorMessages.put("No device linked to this user.",
                "Aucun appareil lié à cet utilisateur.");
        errorMessages.put("Phone number not found.",
                "Numéro de téléphone non trouvé.");
        errorMessages.put("Invalid PIN.",
                "PIN incorrect.");
        errorMessages.put("Only one session is allow by user",
                "Une seule session est autorisée par utilisateur. Déconnectez-vous d'abord.");
        errorMessages.put("User is not active.",
                "Votre compte est désactivé. Contactez le support.");
        errorMessages.put("User not found.",
                "Utilisateur non trouvé.");

        // Erreurs générales
        errorMessages.put("Internal server error.",
                "Erreur interne du serveur.");
        errorMessages.put("Bad request.",
                "Requête invalide.");

        // Chercher le message exact dans le body
        for (Map.Entry<String, String> entry : errorMessages.entrySet()) {
            if (errorBody.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Essayer d'extraire le message d'un JSON structuré
        if (errorBody.trim().startsWith("{") && errorBody.trim().endsWith("}")) {
            try {
                JsonObject json = new Gson().fromJson(errorBody, JsonObject.class);
                if (json.has("message")) {
                    String serverMessage = json.get("message").getAsString();
                    return errorMessages.getOrDefault(serverMessage, serverMessage);
                }
                if (json.has("error")) {
                    String serverError = json.get("error").getAsString();
                    return errorMessages.getOrDefault(serverError, serverError);
                }
            } catch (Exception e) {
                Log.e("ERROR_HANDLER", "Error parsing JSON error", e);
            }
        }

        // Si c'est du texte simple, chercher des motifs connus
        if (errorBody.contains("already exists") || errorBody.contains("already exist")) {
            return "Un utilisateur avec ces informations existe déjà.";
        }
        if (errorBody.contains("not found") || errorBody.contains("does not exist")) {
            return "Utilisateur non trouvé.";
        }
        if (errorBody.contains("invalid") || errorBody.contains("incorrect")) {
            return "Informations d'identification incorrectes.";
        }
        if (errorBody.contains("required") || errorBody.contains("mandatory")) {
            return "Certains champs obligatoires sont manquants.";
        }

        return getDefaultErrorMessage(httpCode);
    }

    private static String getDefaultErrorMessage(int httpCode) {
        switch (httpCode) {
            case 400:
                return "Données invalides. Vérifiez les informations saisies.";
            case 401:
                return "Non autorisé. Identifiants incorrects.";
            case 403:
                return "Accès refusé. Vous n'avez pas les permissions nécessaires.";
            case 404:
                return "Ressource non trouvée.";
            case 409:
                return "Un conflit est survenu. Cette ressource existe déjà.";
            case 422:
                return "Données de validation incorrectes.";
            case 500:
                return "Erreur interne du serveur. Veuillez réessayer plus tard.";
            case 502:
                return "Serveur indisponible. Veuillez réessayer.";
            case 503:
                return "Service temporairement indisponible.";
            default:
                return "Erreur " + httpCode + ". Veuillez réessayer.";
        }
    }
}