package com.example.akibapay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.akibapay.helper.LoadingDialogHelper;

public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    private LoadingDialogHelper loadingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        loadingHelper = new LoadingDialogHelper(this);

        initView();
    }
    public void initView(){
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), OtpActivity.class);
                //startActivity(i);
                // Avec message par défaut
               // loadingHelper.showLoading();

                // Ou avec message personnalisé
                loadingHelper.showLoading("Chargement des données...");

                // Simuler un chargement
                new Handler().postDelayed(() -> {
                    loadingHelper.hideLoading();
                }, 2000);
            }
        });
    }


}