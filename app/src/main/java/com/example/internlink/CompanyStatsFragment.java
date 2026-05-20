package com.example.internlink;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.internlink.databinding.FragmentCompanyStatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CompanyStatsFragment extends Fragment {

    private FragmentCompanyStatsBinding binding;
    private FirebaseFirestore db;
    private String companyId;
    private final List<InternshipModel> liveJobs = new ArrayList<>();
    private InternshipAdapter liveJobsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCompanyStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        companyId = FirebaseAuth.getInstance().getUid();

        // Dashboard is for monitoring only. Job cards show active/expired status and countdown timer.
        // Applicant review/profile/actions are handled only from the Applicants page.
        liveJobsAdapter = new InternshipAdapter(liveJobs, null, true);
        binding.rvCompanyLiveJobs.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCompanyLiveJobs.setAdapter(liveJobsAdapter);

        loadLiveStats();
    }

    private void loadLiveStats() {
        if (binding == null || companyId == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("internships")
                .whereEqualTo("companyId", companyId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (binding == null) return;
                    int totalJobs = snapshot.size();
                    int activeJobs = 0;
                    int cachedApplicants = 0;
                    long now = System.currentTimeMillis();
                    List<String> internshipIds = new ArrayList<>();
                    liveJobs.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        InternshipModel internship = doc.toObject(InternshipModel.class);
                        internship.setId(doc.getId());
                        boolean expired = internship.getExpiresAt() > 0 && internship.getExpiresAt() <= now;
                        if (expired && !"EXPIRED".equalsIgnoreCase(internship.getStatus())) {
                            internship.setStatus("EXPIRED");
                            Map<String, Object> update = new HashMap<>();
                            update.put("status", "EXPIRED");
                            db.collection("internships").document(doc.getId()).update(update);
                        }
                        liveJobs.add(internship);
                        internshipIds.add(doc.getId());

                        boolean active = !expired && "ACTIVE".equalsIgnoreCase(internship.getStatus());
                        if (active) activeJobs++;
                        Long applicantCount = doc.getLong("applicantCount");
                        if (applicantCount != null) cachedApplicants += applicantCount.intValue();
                    }

                    if (liveJobsAdapter != null) liveJobsAdapter.updateList(liveJobs);
                    binding.tvEmptyJobs.setVisibility(liveJobs.isEmpty() ? View.VISIBLE : View.GONE);

                    if (totalJobs == 0) {
                        updateStats(0, 0, 0);
                        return;
                    }

                    final int finalTotalJobs = totalJobs;
                    final int finalActiveJobs = activeJobs;
                    final int finalCachedApplicants = cachedApplicants;
                    AtomicInteger pendingCounts = new AtomicInteger(internshipIds.size());
                    AtomicInteger liveApplicants = new AtomicInteger(0);

                    for (String internshipId : internshipIds) {
                        db.collection("internships").document(internshipId).collection("applications")
                                .get()
                                .addOnSuccessListener(appSnapshot -> {
                                    liveApplicants.addAndGet(appSnapshot.size());
                                    if (pendingCounts.decrementAndGet() == 0) {
                                        int applicants = liveApplicants.get() > 0 ? liveApplicants.get() : finalCachedApplicants;
                                        updateStats(finalTotalJobs, finalActiveJobs, applicants);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (pendingCounts.decrementAndGet() == 0) {
                                        updateStats(finalTotalJobs, finalActiveJobs, finalCachedApplicants);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStats.setText("Unable to load live stats. Check your internet connection.");
                    binding.tvEmptyJobs.setVisibility(View.VISIBLE);
                });
    }

    private void updateStats(int totalJobs, int activeJobs, int applicants) {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.GONE);
        binding.tvTotalJobsValue.setText(String.valueOf(totalJobs));
        binding.tvActiveJobsValue.setText(String.valueOf(activeJobs));
        binding.tvApplicantsValue.setText(String.valueOf(applicants));
        binding.tvStats.setText("Live data: " + totalJobs + " jobs posted, " + activeJobs + " active and " + applicants + " applicants received.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
