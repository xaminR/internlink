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

public class UpgradeRequestsFragment extends Fragment {

    private FragmentAdminListBinding binding;
    private FirebaseFirestore db;
    private List<CompanyModel> requestList;
    private UpgradeRequestAdapter adapter;

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
        requestList = new ArrayList<>();
        binding.tvEmpty.setText("No upgrade requests");

        adapter = new UpgradeRequestAdapter(requestList, company -> approveUpgrade(company));

        binding.rvAdminList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminList.setAdapter(adapter);

        fetchRequests();
    }

    private void fetchRequests() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("companies")
                .whereEqualTo("upgradeRequestStatus", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CompanyModel company = doc.toObject(CompanyModel.class);
                        company.setUid(doc.getId());
                        requestList.add(company);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(requestList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void approveUpgrade(CompanyModel company) {
        if (company == null || company.getUid() == null) return;
        db.collection("companies").document(company.getUid())
                .update("packageType", company.getRequestedPackage(),
                        "upgradeRequestStatus", "NONE",
                        "postsUsed", 0)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Upgrade Approved", Toast.LENGTH_SHORT).show();
                    fetchRequests();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Upgrade failed", Toast.LENGTH_SHORT).show());
    }
}
