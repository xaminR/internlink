package com.example.internlink;

import android.os.Bundle;
import android.widget.Toast;
import com.example.internlink.databinding.ActivityRateBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RateActivity extends BaseActivity {

    private ActivityRateBinding binding;
    private FirebaseFirestore db;
    private String companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        companyId = getIntent().getStringExtra("companyId");
        String companyName = getIntent().getStringExtra("companyName");

        binding.tvCompanyName.setText(companyName);

        binding.btnSubmit.setOnClickListener(v -> submitRating());
    }

    private void submitRating() {
        float rating = binding.ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> ratingData = new HashMap<>();
        ratingData.put("rating", rating);
        ratingData.put("userId", FirebaseAuth.getInstance().getUid());
        ratingData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("companies").document(companyId).collection("ratings").add(ratingData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
