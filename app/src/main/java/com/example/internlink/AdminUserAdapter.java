package com.example.internlink;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.internlink.databinding.ItemAdminUserBinding;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    public interface OnUserActionListener {
        void onDelete(UserModel user);
    }

    private final List<UserModel> users;
    private final OnUserActionListener listener;

    public AdminUserAdapter(List<UserModel> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAdminUserBinding binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = users.get(position);
        String role = safe(user.getRole(), "USER");
        holder.binding.tvName.setText(role.equalsIgnoreCase("company") ? "Company Account" : role.equalsIgnoreCase("student") ? "Student Account" : "User Account");
        holder.binding.tvEmail.setText(safe(user.getEmail(), "No email"));
        holder.binding.tvRoleStatus.setText("Role: " + role + " • Status: " + safe(user.getVerificationStatus(), "Active"));
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(user);
        });
    }

    @Override
    public int getItemCount() {
        return users == null ? 0 : users.size();
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAdminUserBinding binding;
        ViewHolder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
