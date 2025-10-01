package com.example.akibapay.ui.profile;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

        // Afficher les informations utilisateur
        displayUserInfo();

        // CORRECTION : Vérifier la connexion Internet et gérer le bouton
        checkInternetConnection();

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CORRECTION : Vérifier à nouveau la connexion au moment du clic
                if (isInternetAvailable()) {
                    showLogoutConfirmation();
                } else {
                    Toast.makeText(requireContext(), "Connexion Internet requise pour la déconnexion", Toast.LENGTH_LONG).show();
                }
            }
        });

        return root;
    }

    private void displayUserInfo() {
        // Récupérer l'utilisateur connecté
        com.example.akibapay.models.Users user = prefsManager.getLoggedInUser();

        if (user != null) {
            binding.numero.setText(user.getPhoneNumber());

            // CORRECTION : Adapter l'affichage du rôle et statut pour les String
            if (user.getRole() != null) {
                binding.TxRole.setText(user.getRoleDisplayName());
            } else {
                binding.TxRole.setText("Utilisateur");
            }

            // Afficher le statut si vous avez un TextView pour ça
            if (binding.connectionStatus != null && user.getStatus() != null) {
                binding.connectionStatus.setText(getStatusDisplayText(user.getStatus()));
            }
        }
    }

    // CORRECTION : Méthodes pour gérer les String au lieu des int
    private String getRoleDisplayText(String role) {
        if (role == null) return "Utilisateur";
        switch (role.toLowerCase()) {
            case "caissier": return "Caissier";
            case "admin": return "Administrateur";
            case "superadmin": return "Super Administrateur";
            default: return role;
        }
    }

    private String getStatusDisplayText(String status) {
        if (status == null) return "Inactif";
        switch (status.toLowerCase()) {
            case "active": return "Actif";
            case "inactive": return "Inactif";
            case "suspended": return "Suspendu";
            case "deleted": return "Supprimé";
            default: return status;
        }
    }

    // CORRECTION : Méthode pour vérifier la connexion Internet
    private boolean isInternetAvailable() {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) requireContext().getSystemService(requireContext().CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // CORRECTION : Méthode pour gérer l'état du bouton selon la connexion
    private void checkInternetConnection() {
        boolean isConnected = isInternetAvailable();

        // Désactiver le bouton si pas de connexion
        binding.btnLogout.setEnabled(isConnected);

        // Changer l'apparence visuelle
        if (isConnected) {
            binding.btnLogout.setAlpha(1.0f);
            binding.btnLogout.setText("Se déconnecter");
        } else {
            binding.btnLogout.setAlpha(0.5f);
            binding.btnLogout.setText("Déco. (Hors ligne)");
        }

        // Optionnel : Afficher un indicateur de statut
        if (binding.connectionStatus != null) {
            if (isConnected) {
                binding.connectionStatus.setText("Connecté");
                binding.connectionStatus.setTextColor(getResources().getColor(R.color.green));
            } else {
                binding.connectionStatus.setText("Hors ligne");
                binding.connectionStatus.setTextColor(getResources().getColor(R.color.red));
            }
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
        // CORRECTION : Vérifier une dernière fois la connexion
        if (!isInternetAvailable()) {
            Toast.makeText(requireContext(),
                    "Impossible de se déconnecter : connexion Internet perdue",
                    Toast.LENGTH_LONG).show();
            checkInternetConnection(); // Mettre à jour l'interface
            return;
        }

        loadingHelper.showLoading("Déconnexion...");

        prefsManager.logoutUser(requireContext(), new PrefsManager.LogoutCallback() {
            @Override
            public void onLogoutComplete(boolean success) {
                loadingHelper.hideLoading();

                if (success) {
                    Toast.makeText(requireContext(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    Toast.makeText(requireContext(),
                            "Erreur lors de la déconnexion. Vérifiez votre connexion.",
                            Toast.LENGTH_LONG).show();
                    checkInternetConnection(); // Mettre à jour l'interface
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

    // CORRECTION : Surveiller les changements de connexion
    @Override
    public void onResume() {
        super.onResume();
        // Vérifier la connexion à chaque fois que le fragment redevient visible
        checkInternetConnection();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void onStart() {
        super.onStart();
        registerNetworkCallback();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterNetworkCallback();
    }

    private void registerNetworkCallback() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) requireContext().getSystemService(requireContext().CONNECTIVITY_SERVICE);

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(android.net.Network network) {
                    requireActivity().runOnUiThread(() -> checkInternetConnection());
                }

                @Override
                public void onLost(android.net.Network network) {
                    requireActivity().runOnUiThread(() -> checkInternetConnection());
                }
            };

            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private void unregisterNetworkCallback() {
        if (networkCallback != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) requireContext().getSystemService(requireContext().CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}