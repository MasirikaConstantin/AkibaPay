package com.example.akibapay.ui.transaction;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.akibapay.adapter.PaymentAdapter;
import com.example.akibapay.api.ApiService;
import com.example.akibapay.api.RetrofitClient;
import com.example.akibapay.databinding.FragmentTransactionsBinding;
import com.example.akibapay.models.Payments;
import com.example.akibapay.utils.ErrorHandler;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;
    private RecyclerView recyclerView;
    private PaymentAdapter adapter;
    private List<Payments> allPayments = new ArrayList<>();
    private List<Payments> filteredPayments = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private EditText editTextSearch;
    private LinearLayout filterLayout; // CHANGEMENT : Renommé
    private TextView filterText; // CHANGEMENT : Ajouté

    private Date startDateFilter = null;
    private Date endDateFilter = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final String TAG = "TransactionsFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TransactionsViewModel transactionsViewModel =
                new ViewModelProvider(this).get(TransactionsViewModel.class);

        binding = FragmentTransactionsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialiser les vues
        initializeViews();
        initializeRecyclerView();
        setupSearchAndFilter();
        loadAllTransactions();
        return root;
    }

    private void initializeViews() {
        recyclerView = binding.recyclerView;
        progressBar = binding.progressBar;
        emptyStateText = binding.emptyStateText;
        editTextSearch = binding.editTextSearch;
        filterLayout = binding.filterLayout;
        filterText = binding.filterText;

        // Vérifier que les vues sont bien trouvées
        if (recyclerView == null) Log.e(TAG, "recyclerView is null");
        if (progressBar == null) Log.e(TAG, "progressBar is null");
        if (emptyStateText == null) Log.e(TAG, "emptyStateText is null");
        if (editTextSearch == null) Log.e(TAG, "editTextSearch is null");
        if (filterLayout == null) Log.e(TAG, "filterLayout is null");
        if (filterText == null) Log.e(TAG, "filterText is null");
    }

    private void initializeRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PaymentAdapter(filteredPayments);
        recyclerView.setAdapter(adapter);

        // Optionnel: Ajouter une décoration
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
    }

    private void setupSearchAndFilter() {
        // Recherche en temps réel
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filtre par date
        filterLayout.setOnClickListener(v -> showDateRangePicker());
    }

    private void loadAllTransactions() {
        showLoadingState();

        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Payments>> call = apiService.getRecentPayments(300);

        call.enqueue(new Callback<List<Payments>>() {
            @Override
            public void onResponse(Call<List<Payments>> call, Response<List<Payments>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPayments.clear();
                    allPayments.addAll(response.body());

                    // Appliquer les filtres initiaux
                    applyFilters();

                    Log.d(TAG, "Loaded " + allPayments.size() + " transactions");
                } else {
                    String errorMessage = ErrorHandler.getErrorMessage(response);
                    showErrorState("Erreur lors du chargement: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<List<Payments>> call, Throwable t) {
                showErrorState("Erreur de connexion: " + t.getMessage());
            }
        });
    }

    private void applyFilters() {
        filteredPayments.clear();

        String searchQuery = editTextSearch.getText().toString().trim().toLowerCase();

        for (Payments payment : allPayments) {
            boolean matchesSearch = true;
            boolean matchesDate = true;

            // Filtre par recherche
            if (!searchQuery.isEmpty()) {
                matchesSearch = matchesSearchQuery(payment, searchQuery);
            }

            // Filtre par date
            if (startDateFilter != null && endDateFilter != null) {
                matchesDate = matchesDateRange(payment);
            }

            if (matchesSearch && matchesDate) {
                filteredPayments.add(payment);
            }
        }

        // Mettre à jour l'adapter
        adapter.updateData(filteredPayments);

        // Mettre à jour l'état de l'interface
        if (filteredPayments.isEmpty()) {
            if (allPayments.isEmpty()) {
                showEmptyState("Aucune transaction trouvée");
            } else {
                showEmptyState("Aucune transaction ne correspond aux critères");
            }
        } else {
            showDataState();
        }
    }

    private boolean matchesSearchQuery(Payments payment, String query) {
        // Recherche dans le numéro de téléphone
        if (payment.getToNumber() != null && payment.getToNumber().toLowerCase().contains(query)) {
            return true;
        }

        // Recherche dans le numéro d'envoi
        if (payment.getFromNumber() != null && payment.getFromNumber().toLowerCase().contains(query)) {
            return true;
        }

        // Recherche dans la référence marchand
        if (payment.getMerchantReference() != null && payment.getMerchantReference().toLowerCase().contains(query)) {
            return true;
        }

        // Recherche dans le montant
        if (String.valueOf(payment.getAmount()).contains(query)) {
            return true;
        }

        // Recherche dans la devise
        if (payment.getCurrency() != null && payment.getCurrency().toLowerCase().contains(query)) {
            return true;
        }

        return false;
    }

    private boolean matchesDateRange(Payments payment) {
        if (payment.getCreatedAt() == null) return false;

        try {
            // Convertir la date ISO en Date
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date paymentDate = isoFormat.parse(payment.getCreatedAt().replace("+00:00", ""));

            // Vérifier si la transaction est dans l'intervalle
            return !paymentDate.before(startDateFilter) && !paymentDate.after(endDateFilter);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing payment date", e);
            return false;
        }
    }

    private void showDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Sélectionner la période")
                        .setSelection(androidx.core.util.Pair.create(
                                MaterialDatePicker.todayInUtcMilliseconds(),
                                MaterialDatePicker.todayInUtcMilliseconds()
                        ))
                        .build();

        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<androidx.core.util.Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(androidx.core.util.Pair<Long, Long> selection) {
                startDateFilter = new Date(selection.first);
                endDateFilter = new Date(selection.second);

                // Appliquer le filtre de date
                applyFilters();

                // Mettre à jour le texte du bouton filtre
                updateFilterButtonText();
            }
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void updateFilterButtonText() {
        if (startDateFilter != null && endDateFilter != null) {
            String dateRange = dateFormat.format(startDateFilter) + " - " + dateFormat.format(endDateFilter);
            // CHANGEMENT : Utilisation directe de filterText
            filterText.setText(dateRange);
        }
    }

    private void clearDateFilter() {
        startDateFilter = null;
        endDateFilter = null;

        // Réinitialiser le texte du bouton
        filterText.setText("Filtrer");

        // Réappliquer les filtres
        applyFilters();
    }

    private void showLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showDataState() {
        progressBar.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState(String message) {
        progressBar.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
        recyclerView.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        progressBar.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}