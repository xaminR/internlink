package com.example.internlink;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.internlink.databinding.FragmentAppliedInternshipsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AppliedInternshipsFragment extends Fragment {

    private FragmentAppliedInternshipsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<ApplicationModel> applicationList;
    private ApplicationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppliedInternshipsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        applicationList = new ArrayList<>();
        adapter = new ApplicationAdapter(applicationList);

        binding.rvApplied.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvApplied.setAdapter(adapter);

        fetchApplications();
    }

    private void fetchApplications() {
        String uid = mAuth.getUid();
        if (uid == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(uid).collection("applications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    applicationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ApplicationModel app = doc.toObject(ApplicationModel.class);
                        app.setApplicationId(doc.getId());
                        applicationList.add(app);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(applicationList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                });
    }
}
