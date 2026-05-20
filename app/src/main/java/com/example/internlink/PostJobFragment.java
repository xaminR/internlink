package com.example.internlink;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.internlink.databinding.FragmentPostJobBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class PostJobFragment extends Fragment {

    private FragmentPostJobBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CompanyModel company;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostJobBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadCompanyData();

        if (binding != null) {
            binding.btnPost.setOnClickListener(v -> checkAndPost());
        }
    }

    private void loadCompanyData() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("companies").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        company = documentSnapshot.toObject(CompanyModel.class);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load company info", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAndPost() {
        if (binding == null) return;
        
        if (company == null) {
            Toast.makeText(getContext(), "Company data still loading...", Toast.LENGTH_SHORT).show();
            loadCompanyData();
            return;
        }

        if (!"APPROVED".equals(company.getVerificationStatus())) {
            Toast.makeText(getContext(), "Account pending verification", Toast.LENGTH_SHORT).show();
            return;
        }

        resetMonthlyPostsIfNeeded();

        int limit = "GOLD".equals(company.getPackageType()) ? 10 : 2;
        if (company.getPostsUsed() >= limit) {
            showUpgradeDialog();
            return;
        }

        saveJob();
    }

    private void resetMonthlyPostsIfNeeded() {
        if (company == null || mAuth.getUid() == null) return;
        Calendar lastReset = Calendar.getInstance();
        lastReset.setTimeInMillis(company.getPostsResetAt());
        Calendar now = Calendar.getInstance();
        boolean newMonth = lastReset.get(Calendar.YEAR) != now.get(Calendar.YEAR) || lastReset.get(Calendar.MONTH) != now.get(Calendar.MONTH);
        if (company.getPostsResetAt() <= 0 || newMonth) {
            company.setPostsUsed(0);
            company.setPostsResetAt(System.currentTimeMillis());
            Map<String, Object> resetData = new HashMap<>();
            resetData.put("postsUsed", 0);
            resetData.put("postsResetAt", company.getPostsResetAt());
            db.collection("companies").document(mAuth.getUid()).update(resetData);
        }
    }

    private void showUpgradeDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Limit Reached")
                .setMessage("You have reached your posting limit. Upgrade to GOLD for more posts.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void saveJob() {
        if (binding == null || company == null) return;

        String uid = mAuth.getUid();
        if (uid == null) return;

        String title = binding.etTitle.getText().toString().trim();
        String desc = binding.etDescription.getText().toString().trim();
        String skills = binding.etSkills.getText().toString().trim();
        String location = binding.etLocation.getText().toString().trim();
        String duration = binding.etDuration.getText().toString().trim();
        String internshipType = binding.etInternshipType.getText().toString().trim();
        String semesterEligibility = binding.etSemesterEligibility.getText().toString().trim();
        String educationCriteria = binding.etEducationCriteria.getText().toString().trim();

        // Error Handling: Validation
        boolean isValid = true;
        if (TextUtils.isEmpty(title)) {
            binding.etTitle.setError("Title is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(desc)) {
            binding.etDescription.setError("Description is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(location)) {
            binding.etLocation.setError("Location is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(skills)) {
            binding.etSkills.setError("Required skills are required");
            isValid = false;
        }
        if (TextUtils.isEmpty(duration)) {
            binding.etDuration.setError("Duration is required");
            isValid = false;
        }

        if (!isValid) return;

        binding.btnPost.setEnabled(false);
        Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();

        try {
            int expiryDays = "GOLD".equals(company.getPackageType()) ? 14 : 7;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, expiryDays);

            InternshipModel internship = new InternshipModel(
                    null,
                    title,
                    company.getCompanyName(),
                    uid,
                    desc,
                    location,
                    duration,
                    calendar.getTimeInMillis(),
                    "ACTIVE"
            );
            internship.setSkillsRequired(skills);
            internship.setInternshipType(internshipType);
            internship.setSemesterEligibility(semesterEligibility);
            internship.setEducationCriteria(educationCriteria);
            internship.setCreatedAt(System.currentTimeMillis());

            db.collection("internships").add(internship)
                    .addOnSuccessListener(documentReference -> {
                        String newInternshipId = documentReference.getId();
                        company.setPostsUsed(company.getPostsUsed() + 1);
                        db.collection("companies").document(uid).update("postsUsed", FieldValue.increment(1));
                        createNotificationRequest(newInternshipId, title, company.getCompanyName());
                        createAudienceNotifications(newInternshipId, title, company.getCompanyName());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Internship Posted Successfully. Students and admin will be notified.", Toast.LENGTH_SHORT).show();
                        }
                        clearFields();
                        if (binding != null) binding.btnPost.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        if (binding != null) binding.btnPost.setEnabled(true);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            if (binding != null) binding.btnPost.setEnabled(true);
            Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationRequest(String internshipId, String title, String companyName) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("topic", "jobs");
        notification.put("title", "New Internship Posted");
        notification.put("body", safe(companyName) + " posted " + safe(title));
        notification.put("internshipId", internshipId);
        notification.put("createdAt", System.currentTimeMillis());
        notification.put("status", "PENDING_SERVER_DELIVERY");
        db.collection("notificationRequests").add(notification);
    }

    private void createAudienceNotifications(String internshipId, String title, String companyName) {
        String notificationTitle = "New Internship Posted";
        String notificationBody = safe(companyName) + " posted " + safe(title);
        addNotificationsForRole("Student", internshipId, notificationTitle, notificationBody);
        addNotificationsForRole("Admin", internshipId, notificationTitle, notificationBody);
    }

    private void addNotificationsForRole(String role, String internshipId, String title, String body) {
        db.collection("users")
                .whereEqualTo("role", role)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot userDoc : queryDocumentSnapshots) {
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("title", title);
                        notification.put("body", body);
                        notification.put("internshipId", internshipId);
                        notification.put("read", false);
                        notification.put("createdAt", System.currentTimeMillis());
                        notification.put("type", "NEW_INTERNSHIP");
                        db.collection("users").document(userDoc.getId()).collection("notifications").add(notification);
                    }
                })
                .addOnFailureListener(e -> Log.w("InternLink", "Failed to create " + role + " notifications", e));
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "InternLink" : value.trim();
    }

    private void clearFields() {
        if (binding == null) return;
        binding.etTitle.setText("");
        binding.etDescription.setText("");
        binding.etSkills.setText("");
        binding.etLocation.setText("");
        binding.etDuration.setText("");
        binding.etInternshipType.setText("");
        binding.etSemesterEligibility.setText("");
        binding.etEducationCriteria.setText("");
        binding.etTitle.setError(null);
        binding.etDescription.setError(null);
        binding.etLocation.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
