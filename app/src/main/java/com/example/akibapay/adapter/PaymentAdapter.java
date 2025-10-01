package com.example.akibapay.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.akibapay.R;
import com.example.akibapay.models.Payments;
import com.example.akibapay.ui.transaction.TransactionDetailActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private static List<Payments> paymentList;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public PaymentAdapter(List<Payments> paymentList) {
        this.paymentList = paymentList;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        Payments payment = paymentList.get(position);
        holder.bind(payment);
    }

    @Override
    public int getItemCount() {
        return paymentList != null ? paymentList.size() : 0;
    }

    public void updateData(List<Payments> newPayments) {
        this.paymentList = newPayments;
        notifyDataSetChanged();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        private TextView merchantName;
        private TextView date;
        private TextView time;
        private TextView amount;
        private TextView status;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            merchantName = itemView.findViewById(R.id.walmart);
            date = itemView.findViewById(R.id.today);
            time = itemView.findViewById(R.id.time);
            amount = itemView.findViewById(R.id.some_id);
            status = itemView.findViewById(R.id.status);

            itemView.setOnClickListener(v -> {
                Payments payment = paymentList.get(getAdapterPosition());
                Context context = itemView.getContext();

                Intent intent = new Intent(context, TransactionDetailActivity.class);
                intent.putExtra("TRANSACTION_ID", payment.getId().toString());
                context.startActivity(intent);
            });
        }

        public void bind(Payments payment) {
            // Nom du marchand - utiliser la référence marchand ou le numéro
            if (payment.getMerchantReference() != null && !payment.getMerchantReference().isEmpty()) {
                merchantName.setText(payment.getFromNumber());
            } else {
                // Utiliser le numéro de destination comme fallback
                String toNumber = payment.getToNumber();
                if (toNumber != null && toNumber.length() >= 4) {
                    merchantName.setText("***" + toNumber.substring(toNumber.length() - 4));
                } else {
                    merchantName.setText("Transaction");
                }
            }

            // Date et heure
            if (payment.getCreatedAt() != null) {
                try {
                    // Parser la date ISO 8601
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date createdAt = isoFormat.parse(payment.getCreatedAt().replace("+00:00", ""));

                    // Formater pour l'affichage
                    date.setText(dateFormat.format(createdAt));
                    time.setText(timeFormat.format(createdAt));

                } catch (Exception e) {
                    date.setText("--/--/----");
                    time.setText("--:--");
                }
            } else {
                date.setText("--/--/----");
                time.setText("--:--");
            }

            // Montant avec formatage
            String amountText = String.format(Locale.getDefault(), "%.0f %s",
                    payment.getAmount(), payment.getCurrency());
            amount.setText(amountText);

            // Statut avec couleur - CORRECTION POUR CHAÎNE DE CARACTÈRES
            String statusText = getStatusText(payment.getStatus());
            status.setText(statusText);

            // Changer la couleur selon le statut
            int color = getStatusColor(payment.getStatus());
            status.setTextColor(color);
        }

        // CORRECTION : Maintenant le statut est une chaîne de caractères
        private String getStatusText(String status) {
            if (status == null) {
                return "Inconnu";
            }

            switch (status.toUpperCase()) {
                case "SUCCESS":
                    return "Succès";
                case "FAILED":
                    return "Échec";
                case "PENDING":
                    return "En attente";
                case "CANCELLED":
                    return "Annulé";
                default:
                    return status; // Retourne la valeur originale si non reconnue
            }
        }

        // CORRECTION : Adapter les couleurs pour les nouveaux statuts
        private int getStatusColor(String status) {
            if (status == null) {
                return Color.parseColor("#9E9E9E"); // Gris par défaut
            }

            switch (status.toUpperCase()) {
                case "SUCCESS":
                    return Color.parseColor("#4CAF50"); // Vert pour succès
                case "FAILED":
                    return Color.parseColor("#F44336"); // Rouge pour échec
                case "PENDING":
                    return Color.parseColor("#FFA500"); // Orange pour en attente
                case "CANCELLED":
                    return Color.parseColor("#9E9E9E"); // Gris pour annulé
                default:
                    return Color.parseColor("#9E9E9E"); // Gris par défaut
            }
        }
    }
}