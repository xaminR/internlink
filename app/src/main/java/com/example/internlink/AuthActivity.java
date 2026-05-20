package com.example.internlink;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.widget.Toast;

import com.example.internlink.databinding.ActivityAuthBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class AuthActivity extends BaseActivity {

    private ActivityAuthBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        checkUserPersistence();

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(binding.toolbar, "InternLink", false);

        binding.viewPager.setAdapter(new AuthPagerAdapter(this));

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Login" : "Register");
        }).attach();
    }

    public void switchToRegister() {
        if (binding != null && binding.viewPager != null) {
            binding.viewPager.setCurrentItem(1, true);
        }
    }

    public void switchToLogin() {
        if (binding != null && binding.viewPager != null) {
            binding.viewPager.setCurrentItem(0, true);
        }
    }

    private void checkUserPersistence() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                handleRedirection(user);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Safe handling: if persistence check fails, stay on AuthActivity
                    });
        }
    }

    private void handleRedirection(UserModel user) {
        if (isFinishing()) return;
        Intent intent;
        if ("Admin".equals(user.getRole())) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("Company".equals(user.getRole())) {
            intent = new Intent(this, CompanyDashboardActivity.class);
        } else {
            intent = new Intent(this, StudentHomeActivity.class);
        }
        startActivity(intent);
        finish();
    }

    private static class AuthPagerAdapter extends FragmentStateAdapter {
        public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new LoginFragment() : new RegisterFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
