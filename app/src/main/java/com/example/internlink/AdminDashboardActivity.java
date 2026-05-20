package com.example.internlink;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.internlink.databinding.ActivityAdminDashboardBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;

public class AdminDashboardActivity extends BaseActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1301;
    private ActivityAdminDashboardBinding binding;
    private ListenerRegistration notificationListener;
    private long notificationListenerStartedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(binding.toolbar, "Admin Dashboard", false);
        NotificationHelper.createChannel(this);
        requestNotificationPermissionIfNeeded();
        FirebaseMessaging.getInstance().subscribeToTopic("jobs");
        listenForNewInternshipNotifications();

        if (binding.btnLogout != null) {
            binding.btnLogout.setOnClickListener(v -> logout());
        }

        binding.viewPager.setAdapter(new AdminPagerAdapter(this));

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Verify Companies"); break;
                case 1: tab.setText("Upgrades"); break;
                case 2: tab.setText("Moderation"); break;
                case 3: tab.setText("Users"); break;
            }
        }).attach();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        }
    }

    private void listenForNewInternshipNotifications() {
        notificationListenerStartedAt = System.currentTimeMillis() - 30000;
        notificationListener = FirebaseFirestore.getInstance()
                .collection("notificationRequests")
                .whereEqualTo("topic", "jobs")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    for (com.google.firebase.firestore.DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() != com.google.firebase.firestore.DocumentChange.Type.ADDED) continue;
                        Long createdAt = change.getDocument().getLong("createdAt");
                        if (createdAt != null && createdAt < notificationListenerStartedAt) continue;
                        String title = change.getDocument().getString("title");
                        String body = change.getDocument().getString("body");
                        NotificationHelper.show(this,
                                title == null ? "New Internship Posted" : title,
                                body == null ? "A new internship is available" : body,
                                Math.abs(change.getDocument().getId().hashCode()));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null) notificationListener.remove();
        super.onDestroy();
    }

    private void logout() {
        try {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Menu logout removed to favor the red button in UI
        return true;
    }

    private static class AdminPagerAdapter extends FragmentStateAdapter {
        public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new PendingCompaniesFragment();
                case 1: return new UpgradeRequestsFragment();
                case 2: return new ModerationFragment();
                default: return new UsersFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
