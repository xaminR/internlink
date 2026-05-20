package com.example.internlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyInternshipsFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private final List<InternshipModel> internshipList = new ArrayList<>();
    private InternshipAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_company_jobs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        RecyclerView rvCompanyJobs = view.findViewById(R.id.rvCompanyJobs);

        adapter = new InternshipAdapter(internshipList, internship -> {
            if (getActivity() == null || internship == null || internship.getId() == null) return;
            Intent intent = new Intent(getActivity(), ApplicantsActivity.class);
            intent.putExtra("internshipId", internship.getId());
            intent.putExtra("internshipTitle", internship.getTitle());
            startActivity(intent);
        }, false);

        rvCompanyJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCompanyJobs.setAdapter(adapter);
        fetchMyInternships();
    }

    private void fetchMyInternships() {
        String uid = mAuth.getUid();
        if (uid == null) return;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        db.collection("internships")
                .whereEqualTo("companyId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    internshipList.clear();
                    long now = System.currentTimeMillis();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        InternshipModel internship = doc.toObject(InternshipModel.class);
                        internship.setId(doc.getId());
                        boolean expired = internship.getExpiresAt() > 0 && internship.getExpiresAt() <= now;
                        if (expired && !"EXPIRED".equalsIgnoreCase(internship.getStatus())) {
                            internship.setStatus("EXPIRED");
                            Map<String, Object> update = new HashMap<>();
                            update.put("status", "EXPIRED");
                            db.collection("internships").document(doc.getId()).update(update);
                        }
                        internshipList.add(internship);
                    }
                    adapter.updateList(internshipList);
                    if (tvEmpty != null) tvEmpty.setVisibility(internshipList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (tvEmpty != null) {
                        tvEmpty.setText("Unable to load jobs. Check your internet connection.");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }
}
