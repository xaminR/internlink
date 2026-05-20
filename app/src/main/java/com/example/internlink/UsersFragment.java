package com.example.internlink;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.internlink.databinding.FragmentAdminListBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    private FragmentAdminListBinding binding;
    private FirebaseFirestore db;
    private List<UserModel> users;
    private AdminUserAdapter adapter;

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
        users = new ArrayList<>();
        binding.tvEmpty.setText("No users found");
        adapter = new AdminUserAdapter(users, this::confirmDeleteUser);
        binding.rvAdminList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdminList.setAdapter(adapter);
        fetchUsers();
    }

    private void fetchUsers() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    users.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        UserModel user = doc.toObject(UserModel.class);
                        user.setUid(doc.getId());
                        String role = user.getRole();
                        if (role != null && role.equalsIgnoreCase("admin")) continue;
                        users.add(user);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load users", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDeleteUser(UserModel user) {
        if (getContext() == null || user == null || user.getUid() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User?")
                .setMessage("This will remove this registered user profile from the app database.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(UserModel user) {
        String uid = user.getUid();
        db.collection("users").document(uid).delete()
                .addOnSuccessListener(aVoid -> {
                    String role = user.getRole();
                    if (role != null && role.equalsIgnoreCase("company")) {
                        db.collection("companies").document(uid).delete();
                    } else if (role != null && role.equalsIgnoreCase("student")) {
                        db.collection("students").document(uid).delete();
                    }
                    Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show());
    }
}
