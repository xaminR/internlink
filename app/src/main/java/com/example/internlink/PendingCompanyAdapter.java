package com.example.internlink;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.internlink.databinding.ItemPendingCompanyBinding;
import java.util.List;

public class PendingCompanyAdapter extends RecyclerView.Adapter<PendingCompanyAdapter.ViewHolder> {

    private List<CompanyModel> companies;
    private OnActionClickListener listener;

    public interface OnActionClickListener {
        void onApprove(CompanyModel company);
        void onReject(CompanyModel company);
    }

    public PendingCompanyAdapter(List<CompanyModel> companies, OnActionClickListener listener) {
        this.companies = companies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPendingCompanyBinding binding = ItemPendingCompanyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CompanyModel company = companies.get(position);
        holder.binding.tvCompanyName.setText(company.getCompanyName());
        holder.binding.tvNTN.setText("NTN: " + company.getNtnNumber());

        holder.binding.btnApprove.setOnClickListener(v -> listener.onApprove(company));
        holder.binding.btnReject.setOnClickListener(v -> listener.onReject(company));
    }

    @Override
    public int getItemCount() {
        return companies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemPendingCompanyBinding binding;
        public ViewHolder(ItemPendingCompanyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
