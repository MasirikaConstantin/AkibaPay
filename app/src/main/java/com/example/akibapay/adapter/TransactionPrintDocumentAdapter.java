package com.example.akibapay.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.akibapay.R;
import com.example.akibapay.models.Payments;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionPrintDocumentAdapter extends PrintDocumentAdapter {
    private Context context;
    private Payments payment;
    private PrintAttributes printAttributes;

    public TransactionPrintDocumentAdapter(Context context, Payments payment) {
        this.context = context;
        this.payment = payment;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal, LayoutResultCallback callback,
                         Bundle extras) {

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        // Stocker les attributs d'impression pour utilisation dans onWrite
        this.printAttributes = newAttributes;

        PrintDocumentInfo info = new PrintDocumentInfo.Builder("receipt.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build();

        callback.onLayoutFinished(info, true);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, WriteResultCallback callback) {

        if (payment == null) {
            callback.onWriteFailed("Données de transaction manquantes");
            return;
        }

        if (cancellationSignal.isCanceled()) {
            callback.onWriteCancelled();
            return;
        }

        PdfDocument pdfDocument = null;

        try {
            // Créer un PdfDocument standard au lieu de PrintedPdfDocument
            pdfDocument = new PdfDocument();

            // Définir les dimensions de la page (format A4 en points)
            int pageWidth = 595;  // A4 width en points
            int pageHeight = 842; // A4 height en points

            // Si nous avons des attributs d'impression, utiliser leurs dimensions
            if (printAttributes != null && printAttributes.getMediaSize() != null) {
                PrintAttributes.MediaSize mediaSize = printAttributes.getMediaSize();
                pageWidth = (int) (mediaSize.getWidthMils() * 72f / 1000f); // Convertir mils en points
                pageHeight = (int) (mediaSize.getHeightMils() * 72f / 1000f);
            }

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            Canvas canvas = page.getCanvas();

            // Créer et dessiner la vue du reçu
            View receiptView = createReceiptView();

            // Mesurer et disposer la vue avec les dimensions de la page
            receiptView.measure(
                    View.MeasureSpec.makeMeasureSpec(pageWidth - 40, View.MeasureSpec.EXACTLY), // Marge de 20 de chaque côté
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            receiptView.layout(0, 0, pageWidth - 40, receiptView.getMeasuredHeight());

            // Translater le canvas pour centrer le contenu
            canvas.translate(20, 20);

            // Dessiner la vue sur le canvas
            receiptView.draw(canvas);

            pdfDocument.finishPage(page);

            // Écrire le PDF dans le fichier de destination
            try (FileOutputStream out = new FileOutputStream(destination.getFileDescriptor())) {
                pdfDocument.writeTo(out);
                out.flush();
            }

            callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

        } catch (IOException e) {
            callback.onWriteFailed("Erreur d'écriture: " + e.getMessage());
        } catch (Exception e) {
            callback.onWriteFailed("Erreur: " + e.getMessage());
        } finally {
            if (pdfDocument != null) {
                pdfDocument.close();
            }
        }
    }

    private View createReceiptView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View receiptView = inflater.inflate(R.layout.layout_receipt, null);

        // Remplir les données du reçu
        TextView tvAmount = receiptView.findViewById(R.id.receipt_amount);
        TextView tvCurrency = receiptView.findViewById(R.id.receipt_currency);
        TextView tvStatus = receiptView.findViewById(R.id.receipt_status);
        TextView tvToNumber = receiptView.findViewById(R.id.receipt_to_number);
        TextView tvMerchantRef = receiptView.findViewById(R.id.receipt_merchant_ref);
        TextView tvCreatedAt = receiptView.findViewById(R.id.receipt_created_at);
        TextView tvTransactionId = receiptView.findViewById(R.id.receipt_transaction_id);

        // Formater les données
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String createdAt = "Date invalide";

        if (payment.getCreatedAt() != null) {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = isoFormat.parse(payment.getCreatedAt().replace("+00:00", ""));
                if (date != null) {
                    createdAt = dateFormat.format(date);
                }
            } catch (Exception e) {
                createdAt = "Date invalide";
            }
        }

        // Remplir les vues avec vérification null
        if (tvAmount != null) {
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.FRANCE); // ou Locale.getDefault()
            nf.setMinimumFractionDigits(2);
            nf.setMaximumFractionDigits(2);

            String formattedAmount = nf.format(payment.getAmount());
            tvAmount.setText(formattedAmount);
        }
        if (tvCurrency != null) {
            tvCurrency.setText(payment.getCurrency() != null ? payment.getCurrency() : "");
        }
        if (tvStatus != null) {
            tvStatus.setText(getStatusText(payment.getStatus()));
        }
        if (tvToNumber != null) {
            tvToNumber.setText(payment.getToNumber() != null ? payment.getToNumber() : "N/A");
        }
        if (tvMerchantRef != null) {
            tvMerchantRef.setText(payment.getMerchantReference() != null ? payment.getMerchantReference() : "N/A");
        }
        if (tvCreatedAt != null) {
            tvCreatedAt.setText(createdAt);
        }
        if (tvTransactionId != null) {
            tvTransactionId.setText(payment.getId() != null ? payment.getId().toString() : "N/A");
        }

        return receiptView;
    }

    private String getStatusText(Object status) {
        if (status == null) {
            return "Inconnu";
        }

        // CORRECTION : Gestion directe des statuts
        if (status instanceof String) {
            String statusStr = (String) status;
            switch (statusStr.toUpperCase()) {
                case "SUCCESS": return "Succès";
                case "FAILED": return "Échec";
                case "PENDING": return "En attente";
                case "CANCELLED": return "Annulé";
                default: return statusStr;
            }
        } else if (status instanceof Integer) {
            int statusInt = (Integer) status;
            switch (statusInt) {
                case 0: return "En attente";
                case 1: return "Succès";
                case 2: return "Échec";
                case 3: return "Annulé";
                default: return "Inconnu";
            }
        }

        return "Inconnu";
    }
}