package com.example.internlink;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", token);
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", token);
        data.put("notificationTopic", "jobs");
        FirebaseFirestore.getInstance().collection("users").document(uid).set(data, SetOptions.merge());
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = "InternLink";
        String body = "You have a new update";
        String internshipId = null;

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null) body = remoteMessage.getNotification().getBody();
        }
        if (!remoteMessage.getData().isEmpty()) {
            if (remoteMessage.getData().get("title") != null) title = remoteMessage.getData().get("title");
            if (remoteMessage.getData().get("body") != null) body = remoteMessage.getData().get("body");
            internshipId = remoteMessage.getData().get("internshipId");
        }
        sendNotification(title, body, internshipId);
    }

    private void sendNotification(String title, String messageBody, String internshipId) {
        NotificationHelper.createChannel(this);
        Intent intent;
        if (internshipId != null && !internshipId.trim().isEmpty()) {
            intent = new Intent(this, InternshipDetailActivity.class);
            intent.putExtra("internshipId", internshipId);
        } else {
            intent = new Intent(this, AuthActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, Math.abs((int) System.currentTimeMillis()), intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        try {
            NotificationManagerCompat.from(this).notify(Math.abs((int) System.currentTimeMillis()), notificationBuilder.build());
        } catch (SecurityException ignored) {
            Log.w("FCM", "Notification permission not granted");
        }
    }
}
