package com.shreechaatcorner.owner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class OwnerMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "shree_chaat_owner_updates";
    private static final String TOPIC = "shree_chaat_owner_orders";

    @Override
    public void onCreate() {
        super.onCreate();
        subscribeOwnerToOrderAlerts();
    }

    @Override
    public void onNewToken(String token) {
        // Topic subscription uses Firebase-managed registration; no token is exposed in the website.
        subscribeOwnerToOrderAlerts();
    }

    private void subscribeOwnerToOrderAlerts() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String title = message.getNotification() != null && message.getNotification().getTitle() != null
                ? message.getNotification().getTitle()
                : message.getData().getOrDefault("title", "Shree Chaat Corner");
        String body = message.getNotification() != null && message.getNotification().getBody() != null
                ? message.getNotification().getBody()
                : message.getData().getOrDefault("body", "You have a new shop update.");
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Shop order updates", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent pending = launch == null ? null : PendingIntent.getActivity(
                this, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(getApplicationInfo().icon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        if (pending != null) notification.setContentIntent(pending);
        manager.notify((int) (System.currentTimeMillis() & 0x7fffffff), notification.build());
    }
}