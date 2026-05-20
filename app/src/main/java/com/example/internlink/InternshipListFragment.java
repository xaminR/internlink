package com.example.internlink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.internlink.databinding.FragmentInternshipListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InternshipListFragment extends Fragment {

    private FragmentInternshipListBinding binding;
    private FirebaseFirestore db;
    private final List<InternshipModel> internshipList = new ArrayList<>();
    private InternshipAdapter adapter;
    private String activeTypeFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInternshipListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        adapter = new InternshipAdapter(new ArrayList<>(), internship -> {
            if (getActivity() == null || internship == null || internship.getId() == null) return;
            Intent intent = new Intent(getActivity(), InternshipDetailActivity.class);
            intent.putExtra("internshipId", internship.getId());
            startActivity(intent);
        });

        binding.rvInternships.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvInternships.setAdapter(adapter);

        fetchInternships();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.chipRemote.setOnClickListener(v -> setTypeFilter(binding.chipRemote.isChecked() ? "REMOTE" : "ALL"));
        binding.chipOnsite.setOnClickListener(v -> setTypeFilter(binding.chipOnsite.isChecked() ? "ONSITE" : "ALL"));
        binding.chipPaid.setOnClickListener(v -> setTypeFilter(binding.chipPaid.isChecked() ? "PAID" : "ALL"));
        binding.tvSeeAll.setOnClickListener(v -> {
            activeTypeFilter = "ALL";
            binding.chipRemote.setChecked(false);
            binding.chipOnsite.setChecked(false);
            binding.chipPaid.setChecked(false);
            binding.etSearch.setText("");
            applyFilters();
        });
    }

    private void fetchInternships() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("internships")
                .whereEqualTo("status", "ACTIVE")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    internshipList.clear();
                    long currentTime = System.currentTimeMillis();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        InternshipModel internship = doc.toObject(InternshipModel.class);
                        if (internship == null) continue;
                        internship.setId(doc.getId());
                        if (internship.getExpiresAt() > 0 && internship.getExpiresAt() <= currentTime) {
                            db.collection("internships").document(doc.getId()).update("status", "EXPIRED");
                        } else {
                            internshipList.add(internship);
                        }
                    }
                    applyFilters();
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvEmpty.setText("Unable to load internships. Please try again.");
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setTypeFilter(String filter) {
        activeTypeFilter = filter == null ? "ALL" : filter;
        binding.chipRemote.setChecked("REMOTE".equals(activeTypeFilter));
        binding.chipOnsite.setChecked("ONSITE".equals(activeTypeFilter));
        binding.chipPaid.setChecked("PAID".equals(activeTypeFilter));
        applyFilters();
    }

    private void applyFilters() {
        if (adapter == null || binding == null) return;
        String query = binding.etSearch.getText() == null ? "" : binding.etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<InternshipModel> filteredList = new ArrayList<>();
        for (InternshipModel item : internshipList) {
            if (item == null) continue;
            String haystack = (safe(item.getTitle()) + " " + safe(item.getCompanyName()) + " " +
                    safe(item.getSkillsRequired()) + " " + safe(item.getLocation()) + " " +
                    safe(item.getDuration()) + " " + safe(item.getInternshipType()) + " " +
                    safe(item.getSemesterEligibility()) + " " + safe(item.getDescription()) + " " +
                    safe(item.getEducationCriteria())).toLowerCase(Locale.ROOT);

            boolean queryMatches = query.isEmpty() || haystack.contains(query);
            boolean typeMatches = matchesSelectedType(item, haystack);
            if (queryMatches && typeMatches) filteredList.add(item);
        }
        adapter.updateList(filteredList);
        if (filteredList.isEmpty()) {
            binding.tvEmpty.setText(query.isEmpty() && "ALL".equals(activeTypeFilter) ? "No active internships found" : "No results found");
            binding.tvEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
        }
    }

    private boolean matchesSelectedType(InternshipModel item, String haystack) {
        if ("ALL".equals(activeTypeFilter)) return true;
        String type = safe(item.getInternshipType()).toLowerCase(Locale.ROOT).replace("-", "").replace(" ", "");
        String location = safe(item.getLocation()).toLowerCase(Locale.ROOT);
        if ("REMOTE".equals(activeTypeFilter)) {
            return type.contains("remote") || location.contains("remote") || haystack.contains("work from home");
        }
        if ("ONSITE".equals(activeTypeFilter)) {
            return type.contains("onsite") || type.contains("on-site") || haystack.contains("onsite") || haystack.contains("on site") || haystack.contains("on-site");
        }
        if ("PAID".equals(activeTypeFilter)) {
            return haystack.contains("paid") || haystack.contains("stipend") || haystack.contains("salary") || haystack.contains("compensation");
        }
        return true;
    }

    private String safe(String s) { return s == null ? "" : s; }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
