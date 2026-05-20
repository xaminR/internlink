package com.example.internlink;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.example.internlink.databinding.ActivityStudentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StudentProfileActivity extends BaseActivity {

    private ActivityStudentProfileBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String localCvPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(binding.toolbar, "My Profile", true);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadProfile();

        if (binding != null) {
            binding.btnSave.setOnClickListener(v -> saveProfile());
            binding.btnViewCV.setOnClickListener(v -> viewCV());
            binding.btnLogout.setOnClickListener(v -> logout());
        }
    }

    private void loadProfile() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).collection("profile").document("info").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (binding == null) return;
                    if (documentSnapshot.exists()) {
                        binding.etName.setText(documentSnapshot.getString("name"));
                        binding.etPhone.setText(documentSnapshot.getString("phone"));
                        binding.etUniversity.setText(documentSnapshot.getString("university"));
                        binding.etSemester.setText(documentSnapshot.getString("semester"));
                        binding.etCourses.setText(documentSnapshot.getString("courses"));
                        binding.etProgrammingLanguages.setText(documentSnapshot.getString("programmingLanguages"));
                        binding.etAddress.setText(documentSnapshot.getString("address"));
                        binding.etHobbies.setText(documentSnapshot.getString("hobbies"));
                        binding.etLinkedIn.setText(documentSnapshot.getString("linkedin"));
                        binding.etGitHub.setText(documentSnapshot.getString("github"));
                        binding.etSkills.setText(documentSnapshot.getString("skills"));
                        binding.etEducation.setText(documentSnapshot.getString("education"));
                        binding.etProjects.setText(documentSnapshot.getString("projects"));
                        localCvPath = documentSnapshot.getString("localCvPath");
                        if (localCvPath != null && !localCvPath.isEmpty()) {
                            binding.btnViewCV.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void saveProfile() {
        if (binding == null) return;
        String uid = mAuth.getUid();
        if (uid == null) return;

        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.tilName.setError("Name is required");
            return;
        }
        binding.tilName.setError(null);
        if (binding.etUniversity.getText().toString().trim().isEmpty()) {
            binding.tilUniversity.setError("University is required");
            return;
        }
        binding.tilUniversity.setError(null);
        if (binding.etSemester.getText().toString().trim().isEmpty()) {
            binding.tilSemester.setError("Semester is required");
            return;
        }
        binding.tilSemester.setError(null);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        Map<String, Object> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("phone", binding.etPhone.getText().toString().trim());
        profile.put("university", binding.etUniversity.getText().toString().trim());
        profile.put("semester", binding.etSemester.getText().toString().trim());
        profile.put("courses", binding.etCourses.getText().toString().trim());
        profile.put("programmingLanguages", binding.etProgrammingLanguages.getText().toString().trim());
        profile.put("address", binding.etAddress.getText().toString().trim());
        profile.put("hobbies", binding.etHobbies.getText().toString().trim());
        profile.put("linkedin", binding.etLinkedIn.getText().toString().trim());
        profile.put("github", binding.etGitHub.getText().toString().trim());
        profile.put("skills", binding.etSkills.getText().toString().trim());
        profile.put("education", binding.etEducation.getText().toString().trim());
        profile.put("projects", binding.etProjects.getText().toString().trim());
        
        if (mAuth.getCurrentUser() != null) {
            profile.put("email", mAuth.getCurrentUser().getEmail());
        }

        try {
            // Generate CV
            String path = CVGenerator.generateCV(this, uid, profile);
            if (path != null) {
                profile.put("localCvPath", path);
                localCvPath = path;
                binding.btnViewCV.setVisibility(View.VISIBLE);
            }

            db.collection("users").document(uid).collection("profile").document("info").set(profile)
                    .addOnSuccessListener(aVoid -> {
                        if (binding != null) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSave.setEnabled(true);
                        }
                        Toast.makeText(this, "Profile Saved & CV Generated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        if (binding != null) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSave.setEnabled(true);
                        }
                        Toast.makeText(this, "Save Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);
            Toast.makeText(this, "An error occurred while saving", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewCV() {
        if (localCvPath == null) {
            Toast.makeText(this, "CV not generated yet", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(localCvPath);
        if (!file.exists()) {
            Toast.makeText(this, "CV file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open CV"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open CV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
