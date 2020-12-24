package com.example.firebasetutorial.classes;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.firebasetutorial.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "ATMChannel";
    public static final String CHANNEL_NAME = "MANAGER";
    public static final String CHANNEL_DEC = "CHANNEL FROM MANAGER";

    //FUNCTION FOR EMPLOYEES TO OPEN A NEW ACTIVITY
    public static void displayNotification(Context context, String title, String body, Class target){

        Intent intent = new Intent(context, target);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, intent, 0);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .setAllowSystemGeneratedContextualActions(true)
                .setColor(Color.BLUE)
                .setVibrate(new long[] { 1000, 1000})
                .setLights(Color.GREEN, 3000, 3000)
                .setWhen(System.currentTimeMillis())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, nBuilder.build());
    }
}
