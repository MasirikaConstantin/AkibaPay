package com.example.akibapay.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.akibapay.R;
import com.example.akibapay.adapter.PaymentAdapter;
import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.databinding.FragmentHomeBinding;
import com.example.akibapay.helper.LoadingDialogHelper;
import com.example.akibapay.models.Payments;
import com.example.akibapay.request.PaymentRequest;
import com.example.akibapay.ui.transaction.TransactionDetailActivity;
import com.example.akibapay.utils.ErrorHandler;
import com.example.akibapay.utils.PrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PaymentAdapter adapter;
    private List<Payments> paymentList = new ArrayList<>();
    private LoadingDialogHelper loadingHelper;
    private PrefsManager prefsManager;

    private static final String TAG = "HomeFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefsManager = PrefsManager.getInstance(getContext());
        loadingHelper = new LoadingDialogHelper(requireContext());

        // Initialiser les vues AVEC LE BINDING
        initializeViews();
        initializeRecyclerView();
        loadRecentPayments();

        // Observer les données du ViewModel si nécessaire
        homeViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            // binding.textHome.setText(text); // Décommentez si vous voulez utiliser le texte
        });

        setupClickListeners();

        return root;
    }

    private void initializeViews() {
        // Vérifier que les vues sont bien trouvées via le binding
        if (binding.progressBar == null) Log.e(TAG, "progressBar is null");
        if (binding.emptyStateText == null) Log.e(TAG, "emptyStateText is null");
        if (binding.recyclerView == null) Log.e(TAG, "recyclerView is null");

        // Définir le nom d'utilisateur
        binding.bonjourNom.setText(prefsManager.getPhoneNumber());
    }

    private void setupClickListeners() {
        binding.initierUneCollecte.setOnClickListener(v -> {
            showPaymentBottomSheet();
        });

        binding.transfertMobile.setOnClickListener(v -> {
            showTransfertBottomSheet();
        });
    }

    private void initializeRecyclerView() {
        Log.d(TAG, "initializeRecyclerView called");

        if (binding.recyclerView != null) {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new PaymentAdapter(paymentList);
            binding.recyclerView.setAdapter(adapter);

            binding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
            Log.d(TAG, "RecyclerView initialized with " + paymentList.size() + " items");
        } else {
            Log.e(TAG, "RecyclerView is null in initializeRecyclerView");
        }
    }

    private void loadRecentPayments() {
        showLoadingState();

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Payments>> call = apiService.getRecentPayments(5);

        call.enqueue(new Callback<List<Payments>>() {
            @Override
            public void onResponse(Call<List<Payments>> call, Response<List<Payments>> response) {
                Log.d(TAG, "API Response received - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Payments> payments = response.body();
                    Log.d(TAG, "Received " + payments.size() + " payments");

                    paymentList.clear();
                    paymentList.addAll(payments);

                    if (adapter != null) {
                        adapter.updateData(paymentList);
                        Log.d(TAG, "Adapter updated with " + paymentList.size() + " items");
                    } else {
                        Log.e(TAG, "Adapter is null when trying to update data");
                    }

                    if (paymentList.isEmpty()) {
                        Log.d(TAG, "No payments found, showing empty state");
                        showEmptyState();
                    } else {
                        Log.d(TAG, "Payments found, showing data state");
                        showDataState();
                    }
                } else {
                    String errorMessage = ErrorHandler.getErrorMessage(response);
                    Log.e(TAG, "API Error: " + errorMessage);
                    showErrorState("Erreur lors du chargement: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<Payments>> call, Throwable t) {
                Log.e(TAG, "API Failure: " + t.getMessage(), t);
                showErrorState("Erreur de connexion: " + t.getMessage());
            }
        });
    }

    private void showLoadingState() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyStateText.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void showDataState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.emptyStateText.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        binding.progressBar.setVisibility(View.GONE);
        binding.emptyStateText.setVisibility(View.VISIBLE);
        binding.emptyStateText.setText("Aucune transaction récente");
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        binding.progressBar.setVisibility(View.GONE);
        binding.emptyStateText.setVisibility(View.VISIBLE);
        binding.emptyStateText.setText(message);
        binding.recyclerView.setVisibility(View.GONE);

        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Pour rafraîchir les données
    public void refreshData() {
        loadRecentPayments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showTransfertBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_transfert, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Configuration du spinner
        Spinner currencySpinner = bottomSheetView.findViewById(R.id.currency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currencies,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        // Gestion du bouton Valider
        Button validateButton = bottomSheetView.findViewById(R.id.validate_buttons);
        validateButton.setOnClickListener(v -> {
            processTransfert();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void processTransfert() {
        Toast.makeText(requireContext(), "Transfert Validé", Toast.LENGTH_SHORT).show();
    }

    private void showPaymentBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_payment, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Configuration du spinner
        Spinner currencySpinner = bottomSheetView.findViewById(R.id.currency_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currencies,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(adapter);

        // Récupérer les éléments pour la gestion du numéro de téléphone
        com.google.android.material.textfield.TextInputEditText phoneInput =
                bottomSheetView.findViewById(R.id.phone_input);
        com.google.android.material.textfield.TextInputEditText amountInput =
                bottomSheetView.findViewById(R.id.amount_input);
        com.google.android.material.imageview.ShapeableImageView imageReseaux =
                bottomSheetView.findViewById(R.id.imageReseaux);

        // Ajouter un TextWatcher pour détecter les changements dans le numéro
        phoneInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateNetworkIcon(s.toString(), imageReseaux);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Gestion du bouton Valider
        Button validateButton = bottomSheetView.findViewById(R.id.validate_buttons);
        validateButton.setOnClickListener(v -> {
            // Récupérer les données du formulaire
            String toNumber = Objects.requireNonNull(phoneInput.getText()).toString().trim();
            String amountStr = Objects.requireNonNull(amountInput.getText()).toString().trim();
            String currency = currencySpinner.getSelectedItem().toString();
            String userId = prefsManager.getUserId();

            // Validation des champs
            if (TextUtils.isEmpty(toNumber)) {
                phoneInput.setError("Le numéro de téléphone est requis");
                return;
            }

            if (TextUtils.isEmpty(amountStr) || amountStr.equals("$ 0.00")) {
                amountInput.setError("Le montant est requis");
                return;
            }

            // Nettoyer le montant (enlever "$ " et convertir en double)
            double amount;
            try {
                String cleanAmount = amountStr.replace("$ ", "").trim();
                amount = Double.parseDouble(cleanAmount);
            } catch (NumberFormatException e) {
                amountInput.setError("Montant invalide");
                return;
            }

            if (amount <= 0) {
                amountInput.setError("Le montant doit être supérieur à 0");
                return;
            }

            // Fermer le bottom sheet et processer le paiement
            bottomSheetDialog.dismiss();
            processPayment(toNumber, amount, currency, userId);
        });

        bottomSheetDialog.show();
    }

    private void processPayment(String toNumber, double amount, String currency, String userId) {
        // Créer un BottomSheetDialog personnalisé pour le statut de paiement
        BottomSheetDialog statusBottomSheet = createPaymentStatusBottomSheet();
        statusBottomSheet.show();

        // Créer la requête de paiement
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setToNumber(toNumber);
        paymentRequest.setAmount(amount);
        paymentRequest.setCurrency(currency);
        paymentRequest.setFromNumber(toNumber);
        paymentRequest.setUserId(userId);

        ApiService apiService = RetrofitClient.getApiService();
        Call<Payments> call = apiService.createPayment(paymentRequest);

        call.enqueue(new Callback<Payments>() {
            @Override
            public void onResponse(Call<Payments> call, Response<Payments> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Payments payment = response.body();
                    // CORRECTION : Utiliser payment.getId() directement (c'est un String)
                    monitorPaymentStatus(payment.getId(), statusBottomSheet, 0);
                } else {
                    statusBottomSheet.dismiss();
                    String errorMessage = ErrorHandler.getErrorMessage(response);
                    showPaymentError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Payments> call, Throwable t) {
                statusBottomSheet.dismiss();
                showPaymentError("Erreur de connexion. Vérifiez votre internet.");
            }
        });
    }

    private void showPaymentError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Erreur de paiement")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
    private BottomSheetDialog createPaymentStatusBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_payment_status, null);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.setCancelable(false); // Empêcher la fermeture pendant le traitement

        // Références aux vues
        ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressBar);
        TextView statusText = bottomSheetView.findViewById(R.id.statusText);
        TextView timerText = bottomSheetView.findViewById(R.id.timerText);
        Button cancelButton = bottomSheetView.findViewById(R.id.cancelButton);

        // Mettre à jour le texte initial
        statusText.setText("Initialisation de la transaction...");

        // Gestion du bouton annuler
        cancelButton.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Toast.makeText(requireContext(), "Transaction annulée", Toast.LENGTH_SHORT).show();
        });

        return bottomSheetDialog;
    }



    private void monitorPaymentStatus(String paymentId, BottomSheetDialog statusBottomSheet, int attemptCount) {
        final int MAX_ATTEMPTS = 15; // 30 secondes (15 tentatives × 2 secondes)
        final int DELAY_BETWEEN_ATTEMPTS = 2000; // 2 secondes

        if (attemptCount >= MAX_ATTEMPTS) {
            // CORRECTION : Timeout après 30 secondes - ouvrir les détails de la transaction
            statusBottomSheet.dismiss();
            showPaymentTimeoutAndOpenDetails(paymentId);
            return;
        }

        // Mettre à jour l'interface
        updateStatusBottomSheet(statusBottomSheet, attemptCount);

        // Vérifier le statut après un délai
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ApiService apiService = RetrofitClient.getApiService();
            Call<Payments> call = apiService.getPaymentById(UUID.fromString(paymentId));

            call.enqueue(new Callback<Payments>() {
                @Override
                public void onResponse(Call<Payments> call, Response<Payments> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Payments payment = response.body();
                        String status = payment.getStatus();

                        // CORRECTION : Utilisation simplifiée et robuste
                        if (status != null) {
                            String upperStatus = status.toUpperCase();

                            if (upperStatus.equals("SUCCESS")) {
                                statusBottomSheet.dismiss();
                                showPaymentSuccess(payment);
                                refreshData();
                            } else if (upperStatus.equals("FAILED") || upperStatus.equals("CANCELLED")) {
                                statusBottomSheet.dismiss();
                                showPaymentFailed(payment);
                            } else {
                                // PENDING ou autre - continuer le monitoring
                                monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                            }
                        } else {
                            // Statut null - continuer le monitoring
                            monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                        }
                    } else {
                        // Erreur API - continuer le monitoring
                        monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                    }
                }

                @Override
                public void onFailure(Call<Payments> call, Throwable t) {
                    // Erreur réseau - continuer le monitoring
                    monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                }
            });
        }, DELAY_BETWEEN_ATTEMPTS);
    }
    // CORRECTION : Nouvelle méthode pour gérer le timeout avec ouverture des détails
    private void showPaymentTimeoutAndOpenDetails(String paymentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Délai dépassé ⏰")
                .setMessage("Aucune réponse du client après 30 secondes. La transaction est en attente de confirmation. Voulez-vous voir les détails de la transaction ?")
                .setPositiveButton("Voir les détails", (dialog, which) -> {
                    // Ouvrir les détails de la transaction
                    openTransactionDetails(paymentId);
                })
                .setNegativeButton("Fermer", null)
                .setCancelable(false)
                .show();
    }

    private void openTransactionDetails(String paymentId) {
        try {
            Intent intent = new Intent(requireContext(), TransactionDetailActivity.class);
            intent.putExtra("TRANSACTION_ID", paymentId);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'ouverture des détails de transaction", e);
            Toast.makeText(requireContext(), "Erreur lors de l'ouverture des détails", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPaymentSuccess(Object status) {
        if (status == null) return false;

        if (status instanceof String) {
            String statusStr = (String) status;
            return "SUCCESS".equalsIgnoreCase(statusStr);
        } else if (status instanceof Integer) {
            int statusInt = (Integer) status;
            return statusInt == 1; // Ancien code pour succès
        }

        return false;
    }

    private boolean isPaymentFailed(Object status) {
        if (status == null) return false;

        if (status instanceof String) {
            String statusStr = (String) status;
            return "FAILED".equalsIgnoreCase(statusStr) || "CANCELLED".equalsIgnoreCase(statusStr);
        } else if (status instanceof Integer) {
            int statusInt = (Integer) status;
            return statusInt == 2 || statusInt == 3; // Ancien code pour échec ou annulé
        }

        return false;
    }

    private boolean isPaymentPending(Object status) {
        if (status == null) return false;

        if (status instanceof String) {
            String statusStr = (String) status;
            return "PENDING".equalsIgnoreCase(statusStr);
        } else if (status instanceof Integer) {
            int statusInt = (Integer) status;
            return statusInt == 0; // Ancien code pour en attente
        }

        return false;
    }



    private void updateStatusBottomSheet(BottomSheetDialog statusBottomSheet, int attemptCount) {
        View bottomSheetView = statusBottomSheet.findViewById(android.R.id.content);
        if (bottomSheetView == null) {
            bottomSheetView = statusBottomSheet.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        }

        if (bottomSheetView != null) {
            TextView statusText = bottomSheetView.findViewById(R.id.statusText);
            TextView timerText = bottomSheetView.findViewById(R.id.timerText);
            ProgressBar progressBar = bottomSheetView.findViewById(R.id.progressBar);

            if (statusText != null) {
                String status = "En attente de confirmation du client...";
                if (attemptCount > 0) {
                    status = "Vérification du statut (" + attemptCount + "/15)...";
                }
                statusText.setText(status);
            }

            if (timerText != null) {
                int seconds = attemptCount * 2;
                int remainingSeconds = 30 - seconds;
                timerText.setText("Temps restant: " + Math.max(0, remainingSeconds) + "s");
            }

            if (progressBar != null) {
                int progress = (attemptCount * 100) / 15; // 15 tentatives max
                progressBar.setProgress(Math.min(progress, 100));
            }
        }
    }

    private void showPaymentSuccess(Payments payment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Paiement réussi ✅")
                .setMessage("Votre paiement de " + payment.getAmount() + " " + payment.getCurrency() +
                        " vers " + payment.getToNumber() + " a été effectué avec succès.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Optionnel : ouvrir les détails
                    openTransactionDetails(payment.getId().toString());
                })
                .setNegativeButton("Fermer", null)
                .setCancelable(false)
                .show();
    }

    private void showPaymentFailed(Payments payment) {
        String statusMessage = "Le paiement n'a pas abouti.";
        String status = payment.getStatus();

        if (status != null) {
            String upperStatus = status.toUpperCase();
            if (upperStatus.equals("FAILED")) {
                statusMessage = "Le paiement a échoué. Veuillez réessayer.";
            } else if (upperStatus.equals("CANCELLED")) {
                statusMessage = "Le paiement a été annulé.";
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Paiement échoué ❌")
                .setMessage(statusMessage)
                .setPositiveButton("Voir les détails", (dialog, which) -> {
                    openTransactionDetails(payment.getId().toString());
                })
                .setNegativeButton("Fermer", null)
                .show();
    }

    private void showPaymentTimeout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Délai dépassé ⏰")
                .setMessage("Aucune réponse du client après 20 secondes. La transaction est en attente de confirmation.")
                .setPositiveButton("OK", null)
                .show();
    }



    // Méthode pour mettre à jour l'icône du réseau
    private void updateNetworkIcon(String phoneNumber, com.google.android.material.imageview.ShapeableImageView imageReseaux) {
        if (phoneNumber.length() >= 3) {
            String prefix = phoneNumber.substring(0, 3);

            switch (prefix) {
                case "091":
                case "090":
                    imageReseaux.setImageResource(R.drawable.afrimoney);
                    imageReseaux.setVisibility(View.VISIBLE);
                    break;
                case "097":
                case "099":
                case "098":
                    imageReseaux.setImageResource(R.drawable.airtel_money);
                    imageReseaux.setVisibility(View.VISIBLE);
                    break;
                case "081":
                case "082":
                case "083":
                    imageReseaux.setImageResource(R.drawable.mpesa);
                    imageReseaux.setVisibility(View.VISIBLE);
                    break;
                case "080":
                case "085":
                case "084":
                case "089":
                    imageReseaux.setImageResource(R.drawable.orange_money);
                    imageReseaux.setVisibility(View.VISIBLE);
                    break;
                default:
                    imageReseaux.setVisibility(View.GONE);
                    break;
            }
        } else {
            imageReseaux.setVisibility(View.GONE);
        }
    }
}