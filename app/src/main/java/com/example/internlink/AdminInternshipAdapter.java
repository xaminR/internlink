package com.example.internlink;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internlink.databinding.ItemAdminInternshipBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminInternshipAdapter extends RecyclerView.Adapter<AdminInternshipAdapter.ViewHolder> {
    public interface OnActionListener {
        void onReview(InternshipModel internship);
        void onDelete(InternshipModel internship);
    }

    private final List<InternshipModel> internships;
    private final OnActionListener listener;

    public AdminInternshipAdapter(List<InternshipModel> internships, OnActionListener listener) {
        this.internships = internships;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminInternshipBinding binding = ItemAdminInternshipBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InternshipModel item = internships.get(position);
        holder.binding.tvTitle.setText(safe(item.getTitle(), "Untitled Internship"));
        holder.binding.tvCompany.setText(safe(item.getCompanyName(), "Unknown Company"));
        String type = safe(item.getInternshipType(), "Not specified");
        String status = getStatus(item);
        holder.binding.tvMeta.setText("Type: " + type + " • Status: " + status);
        holder.binding.tvExpiry.setText("Expiry: " + getExpiryText(item.getExpiresAt()));
        holder.binding.btnReview.setOnClickListener(v -> {
            if (listener != null) listener.onReview(item);
        });
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(item);
        });
    }

    @Override
    public int getItemCount() {
        return internships == null ? 0 : internships.size();
    }

    private String getStatus(InternshipModel item) {
        long expiresAt = item.getExpiresAt();
        if (expiresAt > 0 && expiresAt <= System.currentTimeMillis()) return "Expired";
        String status = item.getStatus();
        return status == null || status.trim().isEmpty() ? "Active" : status.trim();
    }

    private String getExpiryText(long expiresAt) {
        if (expiresAt <= 0) return "Not set";
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(expiresAt));
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdminInternshipBinding binding;
        ViewHolder(ItemAdminInternshipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
