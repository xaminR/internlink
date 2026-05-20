package com.example.internlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.internlink.databinding.FragmentCompanyProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CompanyProfileFragment extends Fragment {

    private FragmentCompanyProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentPackage = "BRONZE";
    private String currentUpgradeStatus = "NONE";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCompanyProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadProfile();

        if (binding != null) {
            binding.btnUpgrade.setOnClickListener(v -> requestUpgrade());
            binding.btnDowngrade.setOnClickListener(v -> confirmDowngrade());
            binding.btnLogout.setOnClickListener(v -> logout());
        }
    }

    private void loadProfile() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("companies").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (binding == null) return;
                    CompanyModel company = documentSnapshot.toObject(CompanyModel.class);
                    if (company != null) {
                        currentPackage = normalizePackage(company.getPackageType());
                        currentUpgradeStatus = normalizeStatus(company.getUpgradeRequestStatus());

                        binding.tvCompanyName.setText(safeText(company.getCompanyName(), "Company Name"));
                        binding.tvNTN.setText("NTN: " + safeText(company.getNtnNumber(), "N/A"));
                        binding.tvStatus.setText(safeText(company.getVerificationStatus(), "PENDING"));
                        binding.tvPackage.setText("Current Package: " + currentPackage);
                        binding.tvPackageInfo.setText(getPackageInfoText(currentPackage));

                        if (getContext() != null) {
                            if ("APPROVED".equalsIgnoreCase(company.getVerificationStatus())) {
                                binding.tvStatus.setTextColor(getContext().getColor(R.color.green_teal));
                            } else if ("REJECTED".equalsIgnoreCase(company.getVerificationStatus())) {
                                binding.tvStatus.setTextColor(getContext().getColor(android.R.color.holo_red_dark));
                            } else {
                                binding.tvStatus.setTextColor(getContext().getColor(R.color.yellow_gold));
                            }
                        }

                        updatePackageButtons();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load company profile", Toast.LENGTH_SHORT).show());
    }

    private void updatePackageButtons() {
        if (binding == null) return;

        if ("GOLD".equals(currentPackage)) {
            binding.btnUpgrade.setEnabled(false);
            binding.btnUpgrade.setText("Gold Package Active");
            binding.btnDowngrade.setVisibility(View.VISIBLE);
            binding.btnDowngrade.setEnabled(true);
            binding.btnDowngrade.setText("Downgrade to Bronze");
            return;
        }

        binding.btnDowngrade.setVisibility(View.GONE);
        if ("PENDING".equals(currentUpgradeStatus)) {
            binding.btnUpgrade.setEnabled(false);
            binding.btnUpgrade.setText("Upgrade Request Pending");
        } else {
            binding.btnUpgrade.setEnabled(true);
            binding.btnUpgrade.setText("Request Package Upgrade (Gold)");
        }
    }

    private void requestUpgrade() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        if ("GOLD".equals(currentPackage)) {
            Toast.makeText(getContext(), "Gold is already the highest package", Toast.LENGTH_SHORT).show();
            updatePackageButtons();
            return;
        }

        if ("PENDING".equals(currentUpgradeStatus)) {
            Toast.makeText(getContext(), "Upgrade request is already pending", Toast.LENGTH_SHORT).show();
            updatePackageButtons();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("upgradeRequestStatus", "PENDING");
        updates.put("requestedPackage", "GOLD");

        db.collection("companies").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    currentUpgradeStatus = "PENDING";
                    Toast.makeText(getContext(), "Upgrade request submitted", Toast.LENGTH_SHORT).show();
                    updatePackageButtons();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmDowngrade() {
        if (!"GOLD".equals(currentPackage)) {
            Toast.makeText(getContext(), "You are already on Bronze package", Toast.LENGTH_SHORT).show();
            updatePackageButtons();
            return;
        }

        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Downgrade Package")
                .setMessage("Your package will change to Bronze instantly. Bronze allows 2 internship posts per month and each post expires in 7 days.")
                .setPositiveButton("Downgrade", (dialog, which) -> downgradeToBronze())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void downgradeToBronze() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("packageType", "BRONZE");
        updates.put("upgradeRequestStatus", "NONE");
        updates.put("requestedPackage", "");
        updates.put("postsUsed", 0);
        updates.put("postsResetAt", System.currentTimeMillis());

        db.collection("companies").document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    currentPackage = "BRONZE";
                    currentUpgradeStatus = "NONE";
                    Toast.makeText(getContext(), "Package downgraded to Bronze", Toast.LENGTH_SHORT).show();
                    if (binding != null) {
                        binding.tvPackage.setText("Current Package: BRONZE");
                        binding.tvPackageInfo.setText(getPackageInfoText("BRONZE"));
                    }
                    updatePackageButtons();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Downgrade failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getPackageInfoText(String packageType) {
        if ("GOLD".equals(packageType)) {
            return "Gold: 10 internship posts/month • Each post expires in 14 days";
        }
        return "Bronze: 2 internship posts/month • Each post expires in 7 days";
    }

    private String normalizePackage(String packageType) {
        if (packageType == null || packageType.trim().isEmpty()) return "BRONZE";
        return packageType.trim().toUpperCase(Locale.US);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) return "NONE";
        return status.trim().toUpperCase(Locale.US);
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.trim();
    }

    private void logout() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
