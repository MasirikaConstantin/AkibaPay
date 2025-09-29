package com.example.akibapay.ui.profile;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.akibapay.LoginActivity;
import com.example.akibapay.R;
import com.example.akibapay.databinding.FragmentProfileBinding;
import com.example.akibapay.utils.PrefsManager;
import com.example.akibapay.helper.LoadingDialogHelper;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private PrefsManager prefsManager;
    private LoadingDialogHelper loadingHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialisation
        prefsManager = PrefsManager.getInstance(requireContext());
        loadingHelper = new LoadingDialogHelper(requireActivity());
        binding.numero.setText(prefsManager.getPhoneNumber());
        // Afficher les informations utilisateur
        displayUserInfo();

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });

        return root;
    }

    private void displayUserInfo() {
        // Récupérer l'utilisateur connecté
        com.example.akibapay.models.Users user = prefsManager.getLoggedInUser();


    }

    private String getRoleText(int roleValue) {
        switch (roleValue) {
            case 0: return "Utilisateur";
            case 1: return "Agent";
            case 2: return "Administrateur";
            default: return "Inconnu";
        }
    }

    private String getStatusText(int statusValue) {
        switch (statusValue) {
            case 0: return "Actif";
            case 1: return "Inactif";
            case 2: return "Suspendu";
            default: return "Inconnu";
        }
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Déconnexion")
                .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> logoutUser())
                .setNegativeButton("Non", null)
                .show();
    }

    private void logoutUser() {
        loadingHelper.showLoading("Déconnexion...");

        prefsManager.logoutUser(requireContext(), new PrefsManager.LogoutCallback() {
            @Override
            public void onLogoutComplete(boolean success) {
                loadingHelper.hideLoading();

                if (success) {
                    Toast.makeText(requireContext(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la déconnexion", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}