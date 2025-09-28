package com.example.akibapay.ui.carte;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.akibapay.databinding.FragmentCartesBinding;

public class CartesFragment extends Fragment {

    private FragmentCartesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CartesViewModel cartesViewModel =
                new ViewModelProvider(this).get(CartesViewModel.class);

        binding = FragmentCartesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //final TextView textView = binding.textNotifications;
        //cartesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}