package com.example.internlink;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internlink.databinding.ItemApplicationBinding;
import java.util.List;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<ApplicationModel> applications;

    public ApplicationAdapter(List<ApplicationModel> applications) {
        this.applications = applications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemApplicationBinding binding = ItemApplicationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationModel application = applications.get(position);
        holder.binding.tvInternshipTitle.setText(application.getInternshipTitle());
        holder.binding.tvCompanyName.setText(application.getCompanyName());
        holder.binding.chipStatus.setText(application.getStatus());

        // Status Colors
        int color;
        switch (application.getStatus()) {
            case "SHORTLISTED": color = Color.parseColor("#4CAF50"); break;
            case "REJECTED": color = Color.parseColor("#F44336"); break;
            case "INTERVIEW": color = Color.parseColor("#2196F3"); break;
            default: color = Color.GRAY; break;
        }
        holder.binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(color));
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemApplicationBinding binding;
        public ViewHolder(ItemApplicationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
