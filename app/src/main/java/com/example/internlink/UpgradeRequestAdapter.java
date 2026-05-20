package com.example.internlink;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internlink.databinding.ItemUpgradeRequestBinding;
import java.util.List;

public class UpgradeRequestAdapter extends RecyclerView.Adapter<UpgradeRequestAdapter.ViewHolder> {

    private List<CompanyModel> requests;
    private OnUpgradeActionListener listener;

    public interface OnUpgradeActionListener {
        void onApprove(CompanyModel company);
    }

    public UpgradeRequestAdapter(List<CompanyModel> requests, OnUpgradeActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUpgradeRequestBinding binding = ItemUpgradeRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompanyModel company = requests.get(position);
        holder.binding.tvCompanyName.setText(company.getCompanyName());
        holder.binding.tvRequest.setText("Requested: " + company.getRequestedPackage());

        holder.binding.btnApprove.setOnClickListener(v -> listener.onApprove(company));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemUpgradeRequestBinding binding;
        public ViewHolder(ItemUpgradeRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
