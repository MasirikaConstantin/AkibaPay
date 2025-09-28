package com.example.akibapay.ui.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.akibapay.databinding.FragmentTransactionsBinding;


public class TransactionsFragment extends Fragment {

    private FragmentTransactionsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TransactionsViewModel transactionsViewModel =
                new ViewModelProvider(this).get(TransactionsViewModel.class);

        binding = FragmentTransactionsBinding.inflate(inflater, container, false);

        transactionsViewModel.getText().observe(getViewLifecycleOwner(), text -> {
            // binding.textHome.setText(text); // Décommentez si vous voulez utiliser le texte
        });
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}