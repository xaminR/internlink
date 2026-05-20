package com.example.internlink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internlink.databinding.ItemInternshipBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InternshipAdapter extends RecyclerView.Adapter<InternshipAdapter.ViewHolder> {

    private final List<InternshipModel> internships = new ArrayList<>();
    private final OnItemClickListener listener;
    private final boolean showTimer;

    public interface OnItemClickListener {
        void onItemClick(InternshipModel internship);
    }

    public InternshipAdapter(List<InternshipModel> internships, OnItemClickListener listener) {
        this(internships, listener, true);
    }

    public InternshipAdapter(List<InternshipModel> internships, OnItemClickListener listener, boolean showTimer) {
        this.listener = listener;
        this.showTimer = showTimer;
        updateList(internships);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInternshipBinding binding = ItemInternshipBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InternshipModel internship = internships.get(position);
        holder.binding.tvTitle.setText(safe(internship.getTitle(), "Untitled Internship"));
        holder.binding.tvCompanyName.setText(safe(internship.getCompanyName(), "Verified Company"));
        holder.binding.tvLocation.setText("Location: " + safe(internship.getLocation(), "Not specified"));
        holder.binding.tvDuration.setText("Duration: " + safe(internship.getDuration(), "Not specified"));

        String skills = safe(internship.getSkillsRequired(), "Not specified");
        holder.binding.tvSkills.setText("Skills: " + skills);

        long now = System.currentTimeMillis();
        boolean expired = internship.getExpiresAt() > 0 && internship.getExpiresAt() <= now;
        String status = expired ? "Expired" : safe(internship.getStatus(), "ACTIVE");
        if ("ACTIVE".equalsIgnoreCase(status)) {
            status = "Active";
        }
        holder.binding.tvStatus.setText("Status: " + status);

        if (internship.getExpiresAt() > 0) {
            String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(internship.getExpiresAt()));
            holder.binding.tvExpiry.setText("Expires: " + date);
            holder.binding.tvTimer.setText(buildTimerText(internship.getExpiresAt(), now));
        } else {
            holder.binding.tvExpiry.setText("Expires: Not set");
            holder.binding.tvTimer.setText("Timer: Not available");
        }

        holder.binding.tvTimer.setVisibility(showTimer ? View.VISIBLE : View.GONE);

        if (listener == null) {
            holder.itemView.setClickable(false);
            holder.itemView.setFocusable(false);
            holder.itemView.setOnClickListener(null);
        } else {
            holder.itemView.setClickable(true);
            holder.itemView.setFocusable(true);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(internship));
        }
    }

    @Override
    public int getItemCount() {
        return internships.size();
    }

    public void updateList(List<InternshipModel> newList) {
        internships.clear();
        if (newList != null) internships.addAll(newList);
        notifyDataSetChanged();
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String buildTimerText(long expiresAt, long now) {
        long diff = expiresAt - now;
        if (diff <= 0) {
            long expiredDays = Math.max(1, TimeUnit.MILLISECONDS.toDays(Math.abs(diff)));
            return "Timer: Expired " + expiredDays + " day(s) ago";
        }
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        if (days > 0) {
            return "Timer: " + days + " day(s) " + hours + " hour(s) left";
        }
        if (hours > 0) {
            return "Timer: " + hours + " hour(s) " + minutes + " minute(s) left";
        }
        return "Timer: " + Math.max(1, minutes) + " minute(s) left";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemInternshipBinding binding;
        public ViewHolder(ItemInternshipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
