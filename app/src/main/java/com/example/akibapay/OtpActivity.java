package com.example.akibapay;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.otpview.OTPListener;
import com.otpview.OTPTextView;

public class OtpActivity extends AppCompatActivity {
    private OTPTextView otpTextView;
    private TextView timerText;
    private Button errorButton;
    private Button successButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);
        initializeViews();
        setupOTPListener();
        setupButtons();
        additionalMethods();
    }


    private void initializeViews() {
        otpTextView = findViewById(R.id.otp_view);
        timerText = findViewById(R.id.timer);
        errorButton = findViewById(R.id.button);
        successButton = findViewById(R.id.btnRegister);
    }

    private void setupOTPListener() {
        otpTextView.requestFocusOTP();
        otpTextView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {
                // Callback lorsque l'utilisateur interagit avec l'OTP
            }

            @Override
            public void onOTPComplete(String otp) {
                Intent intent =  new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Le Code est " + otp, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtons() {
        if (errorButton != null) {
            errorButton.setOnClickListener(v -> otpTextView.showError());
        }

        if (successButton != null) {
            successButton.setOnClickListener(v -> otpTextView.showSuccess());
        }
    }

    private void additionalMethods() {
        // Récupère le listener OTP actuel (null si rien n'est défini)
        OTPListener currentListener = otpTextView.getOtpListener();

        // Définit le focus sur la boîte OTP (n'ouvre pas le clavier)
        otpTextView.requestFocusOTP();

        // Définit l'OTP dans la boîte (pour le cas où l'OTP est récupéré par SMS)
        // String otpString = "123456";
        // otpTextView.setOTP(otpString);

        // Récupère l'OTP saisi par l'utilisateur (fonctionne aussi pour une saisie partielle)
        //String currentOTP = otpTextView.getOTP();

        // Affiche l'état de succès à l'utilisateur
        otpTextView.showSuccess();

        // Affiche l'état d'erreur à l'utilisateur
        otpTextView.showError();

        // Ramène les vues à l'état par défaut
        otpTextView.resetState();
    }

    // Méthode pour gérer le compte à rebours du timer
    private void startTimer() {
        // Implémentation du compte à rebours
        // Vous pouvez utiliser CountDownTimer ici
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nettoyage si nécessaire
    }

}