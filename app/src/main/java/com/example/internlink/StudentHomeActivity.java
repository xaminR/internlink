package com.example.internlink;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.internlink.databinding.ActivityStudentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.HashMap;
import java.util.Map;

public class StudentHomeActivity extends BaseActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 1201;
    private ActivityStudentHomeBinding binding;
    private ListenerRegistration notificationListener;
    private long notificationListenerStartedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar(binding.toolbar, "Student Dashboard", false);
        NotificationHelper.createChannel(this);
        requestNotificationPermissionIfNeeded();
        subscribeStudentForNotifications();
        listenForNewInternshipNotifications();

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new InternshipListFragment());
                return true;
            } else if (id == R.id.nav_applied) {
                loadFragment(new AppliedInternshipsFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, StudentProfileActivity.class));
                return false;
            }
            return false;
        });

        loadFragment(new InternshipListFragment());
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
        }
    }

    private void subscribeStudentForNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("jobs")
                .addOnCompleteListener(task -> Log.d("FCM", task.isSuccessful() ? "Subscribed to jobs topic" : "Topic subscription failed"));

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid == null || token == null || token.trim().isEmpty()) return;
            Map<String, Object> data = new HashMap<>();
            data.put("fcmToken", token);
            data.put("notificationTopic", "jobs");
            FirebaseFirestore.getInstance().collection("users").document(uid).set(data, SetOptions.merge());
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitAllowingStateLoss();
    }
}
