package com.example.internlink;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.Toast;
import com.example.internlink.databinding.ActivityInternshipDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

public class InternshipDetailActivity extends BaseActivity {

    private ActivityInternshipDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String internshipId;
    private InternshipModel internship;
    private boolean alreadyApplied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInternshipDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        internshipId = getIntent().getStringExtra("internshipId");

        if (internshipId == null || internshipId.trim().isEmpty()) {
            Toast.makeText(this, "Internship not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnApply.setEnabled(false);
        binding.btnApply.setText("Loading...");
        loadInternshipDetails();
        checkIfApplied();
        binding.btnApply.setOnClickListener(v -> applyForInternship());
    }

    private void loadInternshipDetails() {
        db.collection("internships").document(internshipId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Internship no longer exists", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    internship = documentSnapshot.toObject(InternshipModel.class);
                    if (internship == null) {
                        Toast.makeText(this, "Invalid internship data", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    internship.setId(documentSnapshot.getId());
                    setupToolbar(binding.toolbar, safe(internship.getTitle()), true);
                    binding.tvTitle.setText(safe(internship.getTitle()));
                    binding.tvCompanyName.setText(safe(internship.getCompanyName()));
                    binding.tvDescription.setText(safe(internship.getDescription()));
                    binding.tvDetails.setText("Location: " + safe(internship.getLocation()) +
                            "\nDuration: " + safe(internship.getDuration()) +
                            "\nRequired Skills: " + safe(internship.getSkillsRequired()) +
                            "\nType: " + safe(internship.getInternshipType()) +
                            "\nSemester Eligibility: " + safe(internship.getSemesterEligibility()) +
                            "\nEducation Criteria: " + safe(internship.getEducationCriteria()));

                    if (isExpired(internship)) {
                        binding.btnApply.setEnabled(false);
                        binding.btnApply.setText("Expired");
                        db.collection("internships").document(internshipId).update("status", "EXPIRED");
                    } else if (!alreadyApplied) {
                        binding.btnApply.setEnabled(true);
                        binding.btnApply.setText("Apply Now");
                    }
                })
                .addOnFailureListener(e -> {
                    binding.btnApply.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load internship: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfApplied() {
        String uid = mAuth.getUid();
        if (uid == null) return;
        db.collection("internships").document(internshipId).collection("applications").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        alreadyApplied = true;
                        binding.btnApply.setEnabled(false);
                        binding.btnApply.setText("Already Applied");
                    }
                });
    }

    private void applyForInternship() {
        String uid = mAuth.getUid();
        if (uid == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }
        if (internship == null) {
            Toast.makeText(this, "Internship data is still loading", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isExpired(internship)) {
            binding.btnApply.setEnabled(false);
            binding.btnApply.setText("Expired");
            Toast.makeText(this, "This internship has expired", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnApply.setEnabled(false);
        db.collection("internships").document(internshipId).collection("applications").document(uid).get()
                .addOnSuccessListener(existing -> {
                    if (existing.exists()) {
                        alreadyApplied = true;
                        binding.btnApply.setText("Already Applied");
                        return;
                    }
                    loadProfileAndSubmit(uid);
                })
                .addOnFailureListener(e -> {
                    binding.btnApply.setEnabled(true);
                    Toast.makeText(this, "Could not verify application status", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProfileAndSubmit(String uid) {
        db.collection("users").document(uid).collection("profile").document("info").get()
                .addOnSuccessListener(profileDoc -> {
                    if (!profileDoc.exists()) {
                        binding.btnApply.setEnabled(true);
                        Toast.makeText(this, "Complete your profile before applying", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String name = profileDoc.getString("name");
                    String skills = profileDoc.getString("skills");
                    String semester = profileDoc.getString("semester");
                    if (!isEligibleBySemester(semester)) {
                        binding.btnApply.setEnabled(true);
                        showEligibilityDialog(semester);
                        return;
                    }

                    ApplicationModel application = new ApplicationModel(
                            uid,
                            internshipId,
                            safe(internship.getTitle()),
                            safe(internship.getCompanyName()),
                            uid,
                            safe(name),
                            safe(skills),
                            "APPLIED",
                            System.currentTimeMillis()
                    );

                    db.collection("internships").document(internshipId).collection("applications").document(uid).set(application)
                            .addOnSuccessListener(aVoid -> {
                                db.collection("users").document(uid).collection("applications").document(internshipId).set(application);
                                db.collection("internships").document(internshipId).update("applicantCount", FieldValue.increment(1));
                                createInAppNotification(uid, "Application submitted", "You applied for " + safe(internship.getTitle()));
                                Toast.makeText(this, "Application sent to company successfully", Toast.LENGTH_SHORT).show();
                                alreadyApplied = true;
                                binding.btnApply.setEnabled(false);
                                binding.btnApply.setText("Already Applied");
                            })
                            .addOnFailureListener(e -> {
                                binding.btnApply.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.btnApply.setEnabled(true);
                    Toast.makeText(this, "Could not read your profile. Complete your profile first.", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isEligibleBySemester(String semester) {
        if (internship == null) return false;
        int studentSemester = extractFirstNumber(semester);
        int requiredSemester = extractFirstNumber(internship.getSemesterEligibility());

        // If no semester requirement is set on the internship, allow the student to apply.
        if (requiredSemester <= 0) return true;

        // If the student's semester is missing or invalid, do not allow the application.
        if (studentSemester <= 0) return false;

        // Eligibility is semester-based only: 5+ means semester 5, 6, 7, 8... are eligible.
        return studentSemester >= requiredSemester;
    }

    private int extractFirstNumber(String value) {
        if (value == null) return 0;
        Matcher matcher = Pattern.compile("\\d+").matcher(value);
        if (!matcher.find()) return 0;
        try {
            return Integer.parseInt(matcher.group());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showEligibilityDialog(String semester) {
        new AlertDialog.Builder(this)
                .setTitle("Not Eligible")
                .setMessage("Your profile does not match this internship eligibility criteria.\n\nYour semester: " + safe(semester) + "\n\nUpdate your profile and try again.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void createInAppNotification(String uid, String title, String body) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", body);
        notification.put("internshipId", internshipId);
        notification.put("read", false);
        notification.put("createdAt", System.currentTimeMillis());
        db.collection("users").document(uid).collection("notifications").add(notification);
    }

    private boolean isExpired(InternshipModel item) {
        return item.getExpiresAt() > 0 && item.getExpiresAt() <= System.currentTimeMillis();
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Not specified" : value.trim();
    }
}
