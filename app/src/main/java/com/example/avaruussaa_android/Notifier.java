package com.example.avaruussaa_android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public class Notifier implements DefaultLifecycleObserver {
    private static int notificationId = 42; // Used for all notifications. Existing notification is updated with new data.
    private static Boolean inForeground = false; // Used to determine if MainActivity is in the foreground, only notify if not.
    private static final String TAG = "notifiertag";

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        Log.d(TAG, "onResume: LIFECYCLE ONRESUME, notifications disabled because app is in the foreground. owner: " + owner);
        inForeground = true;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.d(TAG, "onResume: LIFECYCLE ONSTART, notifications disabled because app is in the foreground. owner: " + owner);
        inForeground = true;
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        Log.d(TAG, "onResume: LIFECYCLE ONPAUSE, notifications enabled because app is in the background. owner: " + owner);
       inForeground = false;
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.d(TAG, "onResume: LIFECYCLE ONSTOP, notifications enabled because app is in the background. owner: " + owner);
        inForeground = false;
    }

    // Sends a notification to the user showing magnetic activity at the currently selected station.
    // This function handles some of the logic deciding whether to show a notification or not. It checks if 1) permission is granted, 2) app is in
    // the background. Caller will have to check whether notifications are enabled in settings and if activity exceeds the threshold set in app
    // settings. This way we can avoid additional dependencies in this class.
    public static void sendNotification(@NonNull String stationName, @NonNull String activity) {
        try {
            Context context = InitApp.getInstance();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            createNotificationChannel(context, notificationManager);

            // If notification permission is not granted, or MainActivity is in the foreground, return without creating the notification.
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            || inForeground) {
                Log.d(TAG, "sendNotification: RETURNING WITHOUT NOTIFYING, no permission / notifications disabled / app is in foreground");
                return;
            }

            Notification notification = buildNotification(context, stationName, activity);

            // We use the same id for all notifications, so if a previous notification exists it will be updated instead.
            notificationManager.notify(notificationId, notification);

        } catch (Exception e) {
            Log.d(TAG, "createNotificationChannel: EXCEPTION: " + e);
            e.printStackTrace();
        }
    }

    // Creates a notification channel on API versions >= 26.
    private static void createNotificationChannel(@NonNull Context context, @NonNull NotificationManagerCompat notificationManager) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = context.getString(R.string.notification_channel_name);
                String description = context.getString(R.string.notification_channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;

                NotificationChannel channel = new NotificationChannel(context.getString(R.string.notification_channel_id), name, importance);

                channel.setDescription(description);
                channel.enableLights(true);
                channel.setLightColor(Color.MAGENTA);
                channel.setVibrationPattern(new long[]{100, 100});

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "createNotificationChannel: channel created");
            }
        } catch (Exception e) {
            Log.d(TAG, "createNotificationChannel: EXCEPTION: " + e);
        }
    }

    // Builds a notification with an intent which brings MainActivity to foreground (or starts it if it has been destroyed).
    private static Notification buildNotification(@NonNull Context context, String stationName, String activity) {
        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.station_icon)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_body, stationName, activity))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
//            .setOnlyAlertOnce(true) // Enable this if notifications turn out to be too annoying.
            .setContentIntent(pendingIntent);

        return builder.build();
    }
}


