package com.example.akibapay.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.akibapay.R;

public class LoadingDialogHelper {
    private AlertDialog loadingDialog;
    private Context context;
    private String defaultMessage;

    public LoadingDialogHelper(Context context) {
        this.context = context;
        this.defaultMessage = context.getString(R.string.chargement_en_cours);
    }

    public LoadingDialogHelper(Context context, String defaultMessage) {
        this.context = context;
        this.defaultMessage = defaultMessage;
    }

    public void showLoading() {
        showLoading(defaultMessage);
    }

    public void showLoading(String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            updateMessage(message);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.progress, null);

        // Personnaliser le message
        TextView textView = view.findViewById(R.id.text_view);
        textView.setText(message);

        builder.setView(view);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    public void updateMessage(String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            TextView textView = loadingDialog.findViewById(R.id.text_view);
            if (textView != null) {
                textView.setText(message);
            }
        }
    }

    public void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        loadingDialog = null;
    }

    public boolean isShowing() {
        return loadingDialog != null && loadingDialog.isShowing();
    }

    public void setCancelable(boolean cancelable) {
        if (loadingDialog != null) {
            loadingDialog.setCancelable(cancelable);
        }
    }
}