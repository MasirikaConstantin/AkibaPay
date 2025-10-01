package com.example.akibapay.ui.transaction;

import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.akibapay.R;
import com.example.akibapay.adapter.StatusConverter;
import com.example.akibapay.adapter.TransactionPrintDocumentAdapter;
import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.databinding.ActivityTransactionDetailBinding;
import com.example.akibapay.helper.PaymentDeleteHelper;
import com.example.akibapay.models.Payments;
import com.example.akibapay.utils.ErrorHandler;
import com.example.akibapay.utils.PrefsManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDetailActivity extends AppCompatActivity {

    private ActivityTransactionDetailBinding binding;
    private PrefsManager prefsManager;
    private String transactionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefsManager = PrefsManager.getInstance(this);

        // Récupérer l'ID de la transaction depuis l'intent
        transactionId = getIntent().getStringExtra("TRANSACTION_ID");
        if (transactionId == null) {
            Toast.makeText(this, "Transaction non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        loadTransactionDetails();
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Détails de la transaction");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadTransactionDetails() {
        showLoadingState();

        ApiService apiService = RetrofitClient.getApiService();
        Call<Payments> call = apiService.getPaymentById(UUID.fromString(transactionId));

        call.enqueue(new Callback<Payments>() {
            @Override
            public void onResponse(Call<Payments> call, Response<Payments> response) {
                hideLoadingState();

                if (response.isSuccessful() && response.body() != null) {
                    Payments payment = response.body();
                    displayTransactionDetails(payment);
                    setupDeleteButton(payment);

                } else {
                    String errorMessage = ErrorHandler.getErrorMessage(response);
                    showErrorState("Erreur lors du chargement: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Payments> call, Throwable t) {
                hideLoadingState();
                showErrorState("Erreur de connexion: " + t.getMessage());
            }
        });
    }

    private void displayTransactionDetails(Payments payment) {
        // Montant et devise
        binding.amountText.setText(String.format(Locale.getDefault(), "%.2f", payment.getAmount()));
        binding.currencyText.setText(payment.getCurrency());

        // CORRECTION : Utiliser StatusConverter pour le texte du statut
        binding.statusText.setText(getStatusText(payment.getStatus()));

        // CORRECTION : Utiliser StatusConverter pour la couleur du statut
        binding.statusText.setTextColor(getStatusColor(payment.getStatus()));

        // Numéros
        binding.toNumberText.setText(payment.getToNumber());

        // Références
        binding.merchantReferenceText.setText(payment.getMerchantReference());

        // Dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (payment.getCreatedAt() != null) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date createdAt = isoFormat.parse(payment.getCreatedAt().replace("+00:00", ""));
                binding.createdAtText.setText(dateFormat.format(createdAt));
            } catch (Exception e) {
                binding.createdAtText.setText("Date invalide");
            }
        }

        if (payment.getUpdatedAt() != null) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date updatedAt = isoFormat.parse(payment.getUpdatedAt().replace("+00:00", ""));
                binding.updatedAtText.setText(dateFormat.format(updatedAt));
            } catch (Exception e) {
                binding.updatedAtText.setText("Date invalide");
            }
        }

        // Bouton d'impression
        com.google.android.material.button.MaterialButton printButton = binding.printButton;
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                String jobName = "Reçu Transaction - " + transactionId;

                PrintAttributes printAttributes = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(new PrintAttributes.Resolution("default", "default", 300, 300))
                        .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                        .build();

                PrintDocumentAdapter printAdapter = new TransactionPrintDocumentAdapter(TransactionDetailActivity.this, payment);

                if (printManager != null) {
                    printManager.print(jobName, printAdapter, printAttributes);
                } else {
                    Toast.makeText(TransactionDetailActivity.this, "Service d'impression non disponible", Toast.LENGTH_SHORT).show();
                }
            }
        });
        showDataState();
    }

    private String formatUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return "N/A";
        }

        if (uuid.length() >= 8) {
            return uuid.substring(0, 8) + "...";
        }

        return uuid;
    }

    private String getStatusText(Object status) {
        return StatusConverter.getStatusText(status);
    }

    // CORRECTION : Méthode pour obtenir la couleur du statut
    private int getStatusColor(Object status) {
        return StatusConverter.getStatusColor(status, this);
    }

    private void showLoadingState() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.contentLayout.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.GONE);
    }

    private void showDataState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.contentLayout.setVisibility(View.VISIBLE);
        binding.errorText.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.contentLayout.setVisibility(View.GONE);
        binding.errorText.setVisibility(View.VISIBLE);
        binding.errorText.setText(message);
    }

    private void hideLoadingState() {
        binding.progressBar.setVisibility(View.GONE);
    }

    private void setupDeleteButton(Payments payment) {
        com.google.android.material.button.MaterialButton deleteButton = binding.deleteButton;

        deleteButton.setOnClickListener(v -> {
            String paymentInfo = PaymentDeleteHelper.formatPaymentInfo(
                    String.format(Locale.getDefault(), "%.2f", payment.getAmount()),
                    payment.getCurrency(),
                    getFormattedDate(payment.getCreatedAt())
            );

            PaymentDeleteHelper.showDeleteConfirmation(
                    TransactionDetailActivity.this,
                    payment.getId().toString(),
                    paymentInfo,
                    new PaymentDeleteHelper.DeleteCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(TransactionDetailActivity.this,
                                    "Transaction supprimée avec succès", Toast.LENGTH_SHORT).show();

                            setResult(RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(TransactionDetailActivity.this,
                                    errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        });
    }

    private String getFormattedDate(String dateString) {
        if (dateString == null) {
            return "Date inconnue";
        }

        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = isoFormat.parse(dateString.replace("+00:00", ""));
            return displayFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }
}