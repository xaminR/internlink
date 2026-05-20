package com.example.internlink;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.internlink.databinding.FragmentAdminListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PendingCompaniesFragment extends Fragment {

    private FragmentAdminListBinding binding;
    private FirebaseFirestore db;
    private List<CompanyModel> companyList;
    private PendingCompanyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        companyList = new ArrayList<>();
        binding.tvEmpty.setText("No pending companies");

        adapter = new PendingCompanyAdapter(companyList, new PendingCompanyAdapter.OnActionClickListener() {
            @Override
            public void onApprove(CompanyModel company) {
                updateStatus(company, "APPROVED");
            }

            @Override
            public void onReject(CompanyModel company) {
                updateStatus(company, "REJECTED");
            }
        });

        binding.rvAdminList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminList.setAdapter(adapter);

        fetchPendingCompanies();
    }

    private void fetchPendingCompanies() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("companies")
                .whereEqualTo("verificationStatus", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    companyList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CompanyModel company = doc.toObject(CompanyModel.class);
                        company.setUid(doc.getId());
                        companyList.add(company);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(companyList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatus(CompanyModel company, String status) {
        if (company == null || company.getUid() == null) return;
        db.collection("companies").document(company.getUid())
                .update("verificationStatus", status)
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(company.getUid()).update("verificationStatus", status);
                    Toast.makeText(getContext(), "Company " + status, Toast.LENGTH_SHORT).show();
                    fetchPendingCompanies();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show());
    }
}
