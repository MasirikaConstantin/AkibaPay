package com.example.akibapay.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.akibapay.R;
import com.example.akibapay.models.Payments;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<String> items;

    public MyAdapter(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mouvement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Configurez vos vues ici avec les données
        holder.walmart.setText("Walmart " + (position + 1));
        holder.time.setText("12:" + String.format("%02d", position));
        holder.today.setText("Today");
        holder.someId.setText("ID" + (position + 1));

        // Gestion du clic sur l'item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Action lors du clic
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // Méthode pour mettre à jour les données
    public void updateData(List<Payments> newItems) {
        //this.items = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView walmart, time, today, someId;
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            walmart = itemView.findViewById(R.id.walmart);
            time = itemView.findViewById(R.id.time);
            today = itemView.findViewById(R.id.today);
            someId = itemView.findViewById(R.id.some_id);
            img = itemView.findViewById(R.id.img);
        }
    }
}