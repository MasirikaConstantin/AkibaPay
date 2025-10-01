package com.example.akibapay.helper;

import android.content.Context;
import android.widget.Toast;

import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.models.ApiResponse;
import com.example.akibapay.utils.ErrorHandler;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentDeleteHelper {

    public interface DeleteCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Supprime une transaction
     */
    public static void deletePayment(Context context, String paymentId, DeleteCallback callback) {
        if (paymentId == null || paymentId.isEmpty()) {
            callback.onError("ID de transaction invalide");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<Void> call = apiService.deletePayment(paymentId); // Changé de ApiResponse<Void> à Void

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Succès - 204 No Content est considéré comme réussi
                    callback.onSuccess();
                } else {
                    // Erreur HTTP (4xx, 5xx)
                    String errorMessage = ErrorHandler.getErrorMessage(response);
                    callback.onError("Erreur: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Erreur de connexion: " + t.getMessage());
            }
        });
    }

    /**
     * Affiche une boîte de dialogue de confirmation avant suppression
     */
    public static void showDeleteConfirmation(Context context, String paymentId, String paymentInfo, DeleteCallback callback) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        builder.setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette transaction ?\n" + paymentInfo)
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    deletePayment(context, paymentId, callback);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * Méthode utilitaire pour formater les informations de la transaction
     */
    public static String formatPaymentInfo(String amount, String currency, String date) {
        StringBuilder info = new StringBuilder();
        if (amount != null) {
            info.append("Montant: ").append(amount);
            if (currency != null) {
                info.append(" ").append(currency);
            }
        }
        if (date != null) {
            if (info.length() > 0) info.append("\n");
            info.append("Date: ").append(date);
        }
        return info.toString();
    }
}