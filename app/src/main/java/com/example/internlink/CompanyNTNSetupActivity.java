package com.example.internlink;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.internlink.databinding.ActivityCompanyNtnSetupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class CompanyNTNSetupActivity extends BaseActivity {

    private ActivityCompanyNtnSetupBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompanyNtnSetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(binding.toolbar, "Company Verification", true);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding.btnSubmit.setOnClickListener(v -> submitNTN());
    }

    private void submitNTN() {
        String companyName = binding.etCompanyName.getText().toString().trim();
        String ntn = binding.etNTN.getText().toString().trim();
        String email = binding.etCompanyEmail.getText().toString().trim();
        String website = binding.etWebsite.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String registrationStatus = binding.etRegistrationStatus.getText().toString().trim();
        String yearsOfOperation = binding.etYearsOfOperation.getText().toString().trim();

        if (companyName.isEmpty() || ntn.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill company name, email, phone, address and registration number", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getUid();
        if (uid == null) return;

        CompanyModel company = new CompanyModel(uid, companyName, ntn, "PENDING");
        company.setEmail(email);
        company.setWebsite(website);
        company.setPhone(phone);
        company.setAddress(address);
        company.setRegistrationStatus(registrationStatus.isEmpty() ? "PENDING" : registrationStatus);
        company.setYearsOfOperation(yearsOfOperation);

        binding.btnSubmit.setEnabled(false);
        db.collection("companies").document(uid).set(company)
                .addOnSuccessListener(aVoid -> {
                    binding.tvStatus.setText("Pending Admin Approval. Access Restricted.");
                    binding.tvStatus.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Submitted for verification", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
