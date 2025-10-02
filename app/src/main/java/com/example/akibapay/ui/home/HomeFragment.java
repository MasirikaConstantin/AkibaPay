package com.example.akibapay.ui.home;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.akibapay.R;
import com.example.akibapay.adapter.PaymentAdapter;
import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.databinding.FragmentHomeBinding;
import com.example.akibapay.helper.LoadingDialogHelper;
import com.example.akibapay.models.DailyStats;
import com.example.akibapay.models.Payments;
import com.example.akibapay.request.PaymentRequest;
import com.example.akibapay.ui.transaction.TransactionDetailActivity;
import com.example.akibapay.utils.ErrorHandler;
import com.example.akibapay.utils.PrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private Map<String, DailyStats> currencyStatsMap = new HashMap<>();
    private String selectedCurrency = "CDF";
    private List<DailyStats> dailyStatsList = new ArrayList<>();
    private static final String TAG = "HomeFragment";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefsManager = PrefsManager.getInstance(getContext());
        loadingHelper = new LoadingDialogHelper(requireContext());

        initializeViews();
        initializeRecyclerView();
        loadRecentPayments();
        setupUI();
        setupClickListeners();

        homeViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            // binding.textHome.setText(text);
        });

        return root;
    }

    private void setupUI() {
        // Configurer le spinner des devises
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currencies,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.currencySpinner.setAdapter(adapter);

        // Ajouter un spinner pour le montant
        setupAmountSpinner();

        // Gérer le changement de devise
        binding.currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCurrency = parent.getItemAtPosition(position).toString();
                updateBalanceDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Ne rien faire
            }
        });
    }

    private void setupAmountSpinner() {
        // Afficher le spinner de chargement pour le montant
        binding.amountProgressBar.setVisibility(View.VISIBLE);
        binding.amountTextView.setVisibility(View.GONE);
        binding.currencySymbol.setVisibility(View.GONE);
    }

    private void hideAmountSpinner() {
        // Cacher le spinner et afficher le montant
        binding.amountProgressBar.setVisibility(View.GONE);
        binding.amountTextView.setVisibility(View.VISIBLE);
        binding.currencySymbol.setVisibility(View.VISIBLE);
    }

    private void loadDailyStats() {
        String userId = prefsManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            showZeroBalance();
            return;
        }

        // Afficher le spinner pendant le chargement
        setupAmountSpinner();

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<DailyStats>> call = apiService.getDailyStats(userId);

        call.enqueue(new Callback<List<DailyStats>>() {
            @Override
            public void onResponse(Call<List<DailyStats>> call, Response<List<DailyStats>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dailyStatsList.clear();
                    currencyStatsMap.clear();

                    List<DailyStats> stats = response.body();
                    dailyStatsList.addAll(stats);

                    // Convertir la liste en Map pour un accès facile
                    for (DailyStats stat : dailyStatsList) {
                        currencyStatsMap.put(stat.getCurrency(), stat);
                    }

                    updateBalanceDisplay();

                    // Log pour débogage
                    Log.d("DailyStats", "Loaded stats: " + dailyStatsList.size() + " currencies");
                    for (DailyStats stat : dailyStatsList) {
                        Log.d("DailyStats", stat.getCurrency() + ": " + stat.getTotalAmount());
                    }
                } else {
                    Log.e("DailyStats", "Error loading stats: " + response.code());
                    showDefaultBalance();
                }

                hideAmountSpinner();
            }

            @Override
            public void onFailure(Call<List<DailyStats>> call, Throwable t) {
                Log.e("DailyStats", "Network error: " + t.getMessage());
                showDefaultBalance();
                hideAmountSpinner();
            }
        });
    }

    private void updateBalanceDisplay() {
        if (currencyStatsMap.containsKey(selectedCurrency)) {
            DailyStats stats = currencyStatsMap.get(selectedCurrency);
            displayBalance(stats);
        } else {
            // Si la devise sélectionnée n'est pas disponible, afficher 0
            showZeroBalance();
        }
    }

    private void displayBalance(DailyStats stats) {
        if (stats != null) {
            String formattedAmount = formatAmount(stats.getTotalAmount(), selectedCurrency);

            // Mettre à jour le montant
            binding.amountTextView.setText(formattedAmount);

            // Mettre à jour le symbole de devise basé sur la sélection du spinner
            updateCurrencySymbol(selectedCurrency);

            // Appliquer la couleur normale pour le montant
            try {
                int textColor = ContextCompat.getColor(requireContext(), R.color.black);
                binding.amountTextView.setTextColor(textColor);
            } catch (Resources.NotFoundException e) {
                binding.amountTextView.setTextColor(Color.BLACK);
            }

            // Log pour débogage
            Log.d("DisplayBalance", "Currency: " + selectedCurrency + ", Amount: " + formattedAmount);
        } else {
            showZeroBalance();
        }
    }

    private void updateCurrencySymbol(String currency) {
        if (binding.currencySymbol != null) {
            switch (currency.toUpperCase()) {
                case "USD":
                    binding.currencySymbol.setText("$");
                    break;
                case "CDF":
                    binding.currencySymbol.setText("FC");
                    break;
                default:
                    binding.currencySymbol.setText(currency);
            }
        }
    }

    private String formatAmount(double amount, String currency) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        // Ajuster le formatage selon la devise
        switch (currency) {
            case "CDF":
                numberFormat.setMinimumFractionDigits(0);
                numberFormat.setMaximumFractionDigits(0);
                break;
            case "USD":
                numberFormat.setMinimumFractionDigits(2);
                numberFormat.setMaximumFractionDigits(2);
                break;
            default:
                numberFormat.setMinimumFractionDigits(2);
                numberFormat.setMaximumFractionDigits(2);
        }

        return numberFormat.format(amount);
    }

    private void showZeroBalance() {
        try {
            binding.amountTextView.setText("0");
            updateCurrencySymbol(selectedCurrency);

            // Appliquer la couleur grise
            int grayColor;
            try {
                grayColor = ContextCompat.getColor(requireContext(), R.color.gray);
            } catch (Resources.NotFoundException e) {
                grayColor = Color.parseColor("#9E9E9E"); // Gray 500 material
            }

            binding.amountTextView.setTextColor(grayColor);

        } catch (Exception e) {
            Log.e("HomeFragment", "Error in showZeroBalance", e);
            binding.amountTextView.setText("0");
            binding.amountTextView.setTextColor(Color.GRAY);
        }
    }

    private void showDefaultBalance() {
        // En cas d'erreur, afficher une valeur par défaut
        binding.amountTextView.setText("--");
        updateCurrencySymbol(selectedCurrency);
        binding.amountTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray));
    }

    private void initializeViews() {
        // Vérifier que les vues sont bien trouvées via le binding
        if (binding.progressBar == null) Log.e(TAG, "progressBar is null");
        if (binding.emptyStateText == null) Log.e(TAG, "emptyStateText is null");
        if (binding.recyclerView == null) Log.e(TAG, "recyclerView is null");

        // Définir le nom d'utilisateur
        binding.bonjourNom.setText(prefsManager.getPhoneNumber());

        // Initialiser l'affichage du montant avec spinner
        setupAmountSpinner();
    }

    private void setupClickListeners() {
        binding.initierUneCollecte.setOnClickListener(v -> {
            showPaymentBottomSheet();
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
        loadDailyStats();
        loadRecentPayments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
        bottomSheetDialog.setCancelable(false);

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
        final int MAX_ATTEMPTS = 15;
        final int DELAY_BETWEEN_ATTEMPTS = 2000;

        if (attemptCount >= MAX_ATTEMPTS) {
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
                                monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                            }
                        } else {
                            monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                        }
                    } else {
                        monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                    }
                }

                @Override
                public void onFailure(Call<Payments> call, Throwable t) {
                    monitorPaymentStatus(paymentId, statusBottomSheet, attemptCount + 1);
                }
            });
        }, DELAY_BETWEEN_ATTEMPTS);
    }

    private void showPaymentTimeoutAndOpenDetails(String paymentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Délai dépassé ⏰")
                .setMessage("Aucune réponse du client après 30 secondes. La transaction est en attente de confirmation. Voulez-vous voir les détails de la transaction ?")
                .setPositiveButton("Voir les détails", (dialog, which) -> {
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
                int progress = (attemptCount * 100) / 15;
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

    @Override
    public void onResume() {
        super.onResume();
        // Rafraîchir les données quand le fragment redevient visible
        loadDailyStats();
        loadRecentPayments();
    }

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