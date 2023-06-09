package com.example.avaruussaa_android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Notifier {
    private static int notificationId = 42;

    public static void sendNotification(@NonNull Context context, @NonNull String stationName, @NonNull String activity) {
        Log.d("notifiertag", "sendNotification: alussa");
        try {
            createNotificationChannel(context);

            // Create an explicit intent for an Activity in your app
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            Resources resources = context.getResources();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
                .setSmallIcon(R.drawable.station_icon_large)
                .setContentTitle(resources.getString(R.string.notification_title))
                .setContentText(resources.getString(R.string.notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            notificationManager.notify(notificationId, builder.build());

        } catch (Exception e) {
            Log.d("notifiertag", "createNotificationChannel: EXCEPTION: " + e);
        }
    }

    private static void createNotificationChannel(@NonNull Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Resources resources = context.getResources();
                CharSequence name = resources.getString(R.string.notification_channel_name);
                String description = resources.getString(R.string.notification_channel_description);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(resources.getString(R.string.notification_channel_id), name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
                Log.d("notifiertag", "createNotificationChannel: channel created");
            }
        } catch (Exception e) {
            Log.d("notifiertag", "createNotificationChannel: EXCEPTION: " + e);
        }
    }
}


