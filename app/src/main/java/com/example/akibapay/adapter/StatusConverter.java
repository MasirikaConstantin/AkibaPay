package com.example.akibapay.adapter;

import android.content.Context;
import android.graphics.Color;

import com.example.akibapay.R;

public class StatusConverter {

    public static String getStatusText(Object status) {
        if (status == null) {
            return "Inconnu";
        }

        if (status instanceof String) {
            String statusStr = (String) status;
            switch (statusStr.toUpperCase()) {
                case "SUCCESS": return "Succès";
                case "FAILED": return "Échec";
                case "PENDING": return "En attente";
                case "CANCELLED": return "Annulé";
                default: return statusStr;
            }
        } else if (status instanceof Integer) {
            int statusInt = (Integer) status;
            switch (statusInt) {
                case 0: return "En attente";
                case 1: return "Succès";
                case 2: return "Échec";
                case 3: return "Annulé";
                default: return "Inconnu";
            }
        }

        return "Inconnu";
    }

    public static int getStatusColor(Object status, Context context) {
        if (status == null) {
            return context.getColor(R.color.gray);
        }

        if (status instanceof String) {
            String statusStr = (String) status;
            switch (statusStr.toUpperCase()) {
                case "SUCCESS":
                    return context.getColor(R.color.green);
                case "FAILED":
                    return context.getColor(R.color.red);
                case "PENDING":
                    return context.getColor(R.color.orange);
                case "CANCELLED":
                    return context.getColor(R.color.gray);
                default:
                    return context.getColor(R.color.gray);
            }
        } else if (status instanceof Integer) {
            int statusInt = (Integer) status;
            switch (statusInt) {
                case 0: return context.getColor(R.color.orange);
                case 1: return context.getColor(R.color.green);
                case 2: return context.getColor(R.color.red);
                case 3: return context.getColor(R.color.gray);
                default: return context.getColor(R.color.gray);
            }
        }

        return context.getColor(R.color.gray);
    }

    // Méthode utilitaire pour la conversion (si vous l'utilisez ailleurs)
    public static String convertStatus(Object status) {
        return getStatusText(status);
    }
}