package com.example.internlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.internlink.databinding.FragmentRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (binding != null) {
            binding.btnRegister.setOnClickListener(v -> registerUser());
            binding.etPassword.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updatePasswordStrength(s == null ? "" : s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
            // No "Already have an account? Login" button was explicitly in the old XML, 
            // but let's assume if it were added, we'd handle it.
        }
    }

    private void registerUser() {
        if (binding == null) return;

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        
        if (binding.spinnerRole.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }
        String role = binding.spinnerRole.getSelectedItem().toString();

        // Error Handling: Reset errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Error Handling: Validation
        boolean isValid = true;
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            isValid = false;
        } else if (!isStrongPassword(password)) {
            binding.tilPassword.setError("Use a stronger password");
            if (binding.tvPasswordStrength != null) {
                binding.tvPasswordStrength.setVisibility(View.VISIBLE);
                binding.tvPasswordStrength.setText(getPasswordGuidance(password));
                binding.tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
            isValid = false;
        }

        if (!isValid) return;

        binding.btnRegister.setEnabled(false);
        Toast.makeText(getContext(), "Creating account...", Toast.LENGTH_SHORT).show();

        try {
            // Hardcoded admin email logic
            final String finalRole = email.equals("admin@internlink.com") ? "Admin" : role;

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        if (authResult.getUser() != null) {
                            String uid = authResult.getUser().getUid();
                            String status = "VERIFIED";
                            if ("Company".equals(finalRole)) {
                                status = "PENDING";
                            }

                            UserModel user = new UserModel(uid, email, finalRole, System.currentTimeMillis(), status);
                            db.collection("users").document(uid).set(user)
                                    .addOnSuccessListener(aVoid -> handleRedirection(user))
                                    .addOnFailureListener(e -> {
                                        if (binding != null) binding.btnRegister.setEnabled(true);
                                        Toast.makeText(getContext(), "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (binding != null) binding.btnRegister.setEnabled(true);
                        String errorMessage = "Registration failed: " + e.getMessage();
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            errorMessage = "An account with this email already exists.";
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            binding.btnRegister.setEnabled(true);
            Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isStrongPassword(String password) {
        if (password == null) return false;
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^A-Za-z0-9].*");
    }

    private void updatePasswordStrength(String password) {
        if (binding == null || binding.tvPasswordStrength == null) return;

        if (TextUtils.isEmpty(password)) {
            binding.tvPasswordStrength.setVisibility(View.GONE);
            binding.tilPassword.setError(null);
            return;
        }

        binding.tvPasswordStrength.setVisibility(View.VISIBLE);
        if (isStrongPassword(password)) {
            binding.tilPassword.setError(null);
            binding.tvPasswordStrength.setText("Strong password");
            binding.tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            binding.tvPasswordStrength.setText(getPasswordGuidance(password));
            binding.tvPasswordStrength.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private String getPasswordGuidance(String password) {
        StringBuilder guidance = new StringBuilder("Weak password. Add ");
        boolean needsComma = false;

        if (password == null || password.length() < 8) {
            guidance.append("at least 8 characters");
            needsComma = true;
        }
        if (password == null || !password.matches(".*[A-Z].*")) {
            if (needsComma) guidance.append(", ");
            guidance.append("uppercase letter");
            needsComma = true;
        }
        if (password == null || !password.matches(".*[a-z].*")) {
            if (needsComma) guidance.append(", ");
            guidance.append("lowercase letter");
            needsComma = true;
        }
        if (password == null || !password.matches(".*[0-9].*")) {
            if (needsComma) guidance.append(", ");
            guidance.append("number");
            needsComma = true;
        }
        if (password == null || !password.matches(".*[^A-Za-z0-9].*")) {
            if (needsComma) guidance.append(", ");
            guidance.append("special character");
        }
        guidance.append(".");
        return guidance.toString();
    }

    private void handleRedirection(UserModel user) {
        if (getActivity() == null) return;
        
        Intent intent;
        if ("Admin".equals(user.getRole())) {
            intent = new Intent(getActivity(), AdminDashboardActivity.class);
        } else if ("Company".equals(user.getRole())) {
            intent = new Intent(getActivity(), CompanyNTNSetupActivity.class);
        } else {
            intent = new Intent(getActivity(), StudentHomeActivity.class);
        }
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
