package com.example.internlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.internlink.databinding.FragmentAdminListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ModerationFragment extends Fragment {

    private FragmentAdminListBinding binding;
    private FirebaseFirestore db;
    private List<InternshipModel> internshipList;
    private AdminInternshipAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        internshipList = new ArrayList<>();
        binding.tvEmpty.setText("No internships found");

        adapter = new AdminInternshipAdapter(internshipList, new AdminInternshipAdapter.OnActionListener() {
            @Override
            public void onReview(InternshipModel internship) {
                if (getContext() == null || internship == null || internship.getId() == null) return;
                Intent intent = new Intent(getContext(), InternshipDetailActivity.class);
                intent.putExtra("internshipId", internship.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(InternshipModel internship) {
                showDeleteDialog(internship);
            }
        });

        binding.rvAdminList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminList.setAdapter(adapter);

        fetchInternships();
    }

    private void fetchInternships() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("internships")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    internshipList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        InternshipModel item = doc.toObject(InternshipModel.class);
                        item.setId(doc.getId());
                        internshipList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(internshipList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load internships", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteDialog(InternshipModel internship) {
        if (getContext() == null || internship == null || internship.getId() == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Internship?")
                .setMessage("This will remove the internship from the platform.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("internships").document(internship.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Internship deleted", Toast.LENGTH_SHORT).show();
                                fetchInternships();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
