package com.example.internlink;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.internlink.databinding.ActivityApplicantsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicantsActivity extends BaseActivity {

    private ActivityApplicantsBinding binding;
    private FirebaseFirestore db;
    private String internshipId;
    private List<ApplicationModel> applicantList;
    private ApplicantAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplicantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        internshipId = getIntent().getStringExtra("internshipId");
        String internshipTitle = getIntent().getStringExtra("internshipTitle");

        setupToolbar(binding.toolbar, internshipTitle == null || internshipTitle.trim().isEmpty() ? "Applicants" : "Applicants - " + internshipTitle, true);

        applicantList = new ArrayList<>();
        adapter = new ApplicantAdapter(applicantList, new ApplicantAdapter.OnApplicantActionListener() {
            @Override
            public void onViewProfile(ApplicationModel applicant) {
                showProfileDialog(applicant);
            }

            @Override
            public void onActionClick(ApplicationModel applicant) {
                showActionDialog(applicant);
            }
        });

        binding.rvApplicants.setLayoutManager(new LinearLayoutManager(this));
        binding.rvApplicants.setAdapter(adapter);

        fetchApplicants();
    }

    private void fetchApplicants() {
        if (internshipId == null || internshipId.trim().isEmpty()) {
            binding.tvEmptyApplicants.setText("Invalid job selected");
            binding.tvEmptyApplicants.setVisibility(android.view.View.VISIBLE);
            return;
        }
        db.collection("internships").document(internshipId).collection("applications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    applicantList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ApplicationModel app = doc.toObject(ApplicationModel.class);
                        app.setApplicationId(doc.getId());
                        applicantList.add(app);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmptyApplicants.setVisibility(applicantList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.tvEmptyApplicants.setText("Unable to load applicants. Check your internet connection.");
                    binding.tvEmptyApplicants.setVisibility(android.view.View.VISIBLE);
                });
    }

    private void showProfileDialog(ApplicationModel applicant) {
        db.collection("users").document(applicant.getStudentId()).collection("profile").document("info").get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String details = "Name: " + doc.getString("name") +
                                "\nEmail: " + doc.getString("email") +
                                "\nPhone: " + doc.getString("phone") +
                                "\nEducation: " + doc.getString("education") +
                                "\nSkills: " + doc.getString("skills") +
                                "\nProjects: " + doc.getString("projects");

                        new AlertDialog.Builder(this)
                                .setTitle("Student Profile")
                                .setMessage(details)
                                .setPositiveButton("Close", null)
                                .show();
                    }
                });
    }

    private void showActionDialog(ApplicationModel applicant) {
        String[] actions = {"Shortlist", "Reject", "Schedule Interview"};
        new AlertDialog.Builder(this)
                .setTitle("Select Action")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) updateStatus(applicant, "SHORTLISTED");
                    else if (which == 1) updateStatus(applicant, "REJECTED");
                    else if (which == 2) {
                        updateStatus(applicant, "INTERVIEW_SCHEDULED");
                        sendInterviewEmail(applicant);
                    }
                })
                .show();
    }

    private void sendInterviewEmail(ApplicationModel applicant) {
        db.collection("users").document(applicant.getStudentId()).collection("profile").document("info").get()
                .addOnSuccessListener(doc -> {
                    String email = doc.getString("email");
                    if (email == null || email.trim().isEmpty()) {
                        Toast.makeText(this, "Student email not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("message/rfc822");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Interview Invitation - " + applicant.getInternshipTitle());
                    intent.putExtra(Intent.EXTRA_TEXT, "Dear " + applicant.getStudentName() + ",\n\nYou have been selected for an interview for " + applicant.getInternshipTitle() + ".\n\nPlease reply to confirm your availability.\n\nRegards,\nInternLink Company Team");
                    try {
                        startActivity(Intent.createChooser(intent, "Send interview email"));
                    } catch (Exception e) {
                        Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Could not load student email", Toast.LENGTH_SHORT).show());
    }

    private void updateStatus(ApplicationModel applicant, String status) {
        // Update in internships/{id}/applications/{uid}
        db.collection("internships").document(internshipId).collection("applications").document(applicant.getStudentId())
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    // Update in users/{uid}/applications/{id}
                    db.collection("users").document(applicant.getStudentId()).collection("applications").document(internshipId)
                            .update("status", status);
                    createApplicantNotification(applicant, status);

                    Toast.makeText(this, "Status updated to " + status, Toast.LENGTH_SHORT).show();
                    fetchApplicants();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    private void createApplicantNotification(ApplicationModel applicant, String status) {
        if (applicant == null || applicant.getStudentId() == null) return;
        String title = "Application Update";
        String message;
        if ("SHORTLISTED".equals(status)) {
            message = "You have been shortlisted for " + safe(applicant.getInternshipTitle());
        } else if ("REJECTED".equals(status)) {
            message = "Your application status was updated for " + safe(applicant.getInternshipTitle());
        } else {
            message = "Interview scheduled for " + safe(applicant.getInternshipTitle());
        }
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", message);
        notification.put("internshipId", internshipId);
        notification.put("read", false);
        notification.put("createdAt", System.currentTimeMillis());
        db.collection("users").document(applicant.getStudentId()).collection("notifications").add(notification);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "Not specified" : value.trim();
    }
}
