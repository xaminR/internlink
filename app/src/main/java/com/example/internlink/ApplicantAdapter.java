package com.example.internlink;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internlink.databinding.ItemApplicantBinding;
import java.util.List;

public class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ViewHolder> {

    private List<ApplicationModel> applicants;
    private OnApplicantActionListener listener;

    public interface OnApplicantActionListener {
        void onViewProfile(ApplicationModel applicant);
        void onActionClick(ApplicationModel applicant);
    }

    public ApplicantAdapter(List<ApplicationModel> applicants, OnApplicantActionListener listener) {
        this.applicants = applicants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemApplicantBinding binding = ItemApplicantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ApplicationModel applicant = applicants.get(position);
        holder.binding.tvStudentName.setText(applicant.getStudentName());
        holder.binding.tvSkills.setText("Skills: " + applicant.getStudentSkills());
        holder.binding.tvStatus.setText(applicant.getStatus());

        holder.binding.btnViewProfile.setOnClickListener(v -> listener.onViewProfile(applicant));
        holder.binding.btnAction.setOnClickListener(v -> listener.onActionClick(applicant));
    }

    @Override
    public int getItemCount() {
        return applicants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemApplicantBinding binding;
        public ViewHolder(ItemApplicantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
