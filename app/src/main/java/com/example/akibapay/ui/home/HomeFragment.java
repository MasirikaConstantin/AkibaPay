package com.example.akibapay.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.akibapay.R;
import com.example.akibapay.adapter.MyAdapter;
import com.example.akibapay.databinding.FragmentHomeBinding;
import com.example.akibapay.utils.PrefsManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private PrefsManager prefsManager;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        prefsManager = PrefsManager.getInstance(getContext());
        // Initialiser le RecyclerView
        initializeRecyclerView();

        // Observer les données du ViewModel si nécessaire
        homeViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            // binding.textHome.setText(text); // Décommentez si vous voulez utiliser le texte
        });
        binding.initierUneCollecte.setOnClickListener(v -> {
            showPaymentBottomSheet();
        });

        binding.transfertMobile.setOnClickListener(v -> {
            showTransfertBottomSheet();
        });
        binding.bonjourNom.setText(prefsManager.getPhoneNumber());
        return root;
    }

    private void initializeRecyclerView() {
        recyclerView = binding.recyclerView;

        // Configurer le LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Créer des données d'exemple
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            items.add("Transaction " + i);
        }

        // Initialiser l'adapter
        adapter = new MyAdapter(items);
        recyclerView.setAdapter(adapter);

        // Optionnel: Ajouter des décorations ou animations
         recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    /*private void showPaymentBottomSheet() {
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

        // Gestion du bouton Valider
        Button validateButton = bottomSheetView.findViewById(R.id.validate_buttons);
        validateButton.setOnClickListener(v -> {
            // Logique de validation
            processPayment();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }*/

    private void processPayment() {
        // Traitement du paiement
        Toast.makeText(requireContext(), "Paiement initié", Toast.LENGTH_SHORT).show();
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
            // Logique de validation
            processTransfert();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
    private void processTransfert() {
        // Traitement du paiement
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
            // Logique de validation
            processPayment();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
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
                    // Orange Money (déjà défini par défaut dans le XML)
                    //imageReseaux.setImageResource(R.drawable.orange_money);
                    imageReseaux.setVisibility(View.GONE);
                    break;
            }
        } else {
            imageReseaux.setVisibility(View.GONE);
        }
    }
}