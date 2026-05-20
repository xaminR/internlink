package com.example.internlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.internlink.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (binding != null) {
            binding.btnLogin.setOnClickListener(v -> loginUser());
            binding.tvRegister.setOnClickListener(v -> {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).switchToRegister();
                }
            });
        }
    }

    private void loginUser() {
        if (binding == null) return;

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Error Handling: Reset errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Error Handling: Input Validation
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
        }

        if (!isValid) return;

        binding.btnLogin.setEnabled(false);
        Toast.makeText(getContext(), "Logging in...", Toast.LENGTH_SHORT).show();

        try {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        if (authResult.getUser() != null) {
                            checkUserRole(authResult.getUser().getUid());
                        } else {
                            binding.btnLogin.setEnabled(true);
                            Toast.makeText(getContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (binding != null) binding.btnLogin.setEnabled(true);
                        String errorMessage = "Login failed: " + e.getMessage();
                        if (e instanceof FirebaseAuthInvalidUserException) {
                            errorMessage = "No account found with this email.";
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Incorrect password.";
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            binding.btnLogin.setEnabled(true);
            Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUserRole(String uid) {
        if (uid == null) return;
        
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            handleRedirection(user);
                        } else {
                            if (binding != null) binding.btnLogin.setEnabled(true);
                            Toast.makeText(getContext(), "User data corrupted", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (binding != null) binding.btnLogin.setEnabled(true);
                        Toast.makeText(getContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding != null) binding.btnLogin.setEnabled(true);
                    Toast.makeText(getContext(), "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleRedirection(UserModel user) {
        if (getActivity() == null) return;
        
        Intent intent;
        if ("Admin".equals(user.getRole())) {
            intent = new Intent(getActivity(), AdminDashboardActivity.class);
        } else if ("Company".equals(user.getRole())) {
            intent = new Intent(getActivity(), CompanyDashboardActivity.class);
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
