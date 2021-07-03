package com.time.scheduling;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.time.scheduling.DataBase.Task;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationHelper {

    private static final String PRIMARY_CHANNEL_ID = "primary_channel_id";
    private static final String TAG = "NotificationHelper";
    private final Context mContext;
    public int mAlarmId;
    private NotificationManager mNotifyManager;

    public NotificationHelper(Context context, int alarmId) {
        this.mContext = context;
        this.mAlarmId = alarmId;
    }


    //----------------------------- Notification Channel -----------------------------------------//

    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        // Pre O does not support / need notification channel
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel notificationChannel = new NotificationChannel(
                PRIMARY_CHANNEL_ID, "Alarmzy", NotificationManager.IMPORTANCE_HIGH);

        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.GREEN);
        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("Notifiche Time Scheduling");
        mNotifyManager.createNotificationChannel(notificationChannel);

        Log.i(TAG, "createNotificationChannel: Channel Created");
    }


    //----------------------------- Deliver Notification -----------------------------------------//

    public Notification deliverNotification(Task task) {


        Intent fullScreenIntent = new Intent(mContext, TaskTriggerActivity.class);
        fullScreenIntent.putExtra("taskId",task.getId());
        fullScreenIntent.putExtra("alarmIdKey", mAlarmId);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(mContext,
                0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Display Alarm Time in notification
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        String formattedTime = sdf.format(System.currentTimeMillis());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext, PRIMARY_CHANNEL_ID)
                .setContentTitle(task.getTitle())
                .setContentText(formattedTime)
                .setSmallIcon(R.drawable.ic_task)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM);
        /* Since on A10 activity cannot be started from service(Including foreground service)
         * Use a High priority notification with FullScreenPendingIntent()
         * Also requires USE_FULL_SCREEN_INTENT permission in manifest
         *
         * This full-screen intent will be launched immediately if device's screen is off
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            builder.setFullScreenIntent(fullScreenPendingIntent, true);
        else
            // Set on notification click intent for pre oreo
            builder.setContentIntent(fullScreenPendingIntent);

        // Return a Notification object to be used by startForeground()
        return builder.build();
    }

    public void deliverPersistentNotification(Task task) {
        String ACTION_DISMISS = BuildConfig.APPLICATION_ID + ".ACTION_DISMISS";
        final String KEY_SNOOZE_LENGTH = "snoozeLength";

        mNotifyManager = (NotificationManager) MyApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        Intent dismissIntent = new Intent(mContext, TaskBroadcastReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        // AlarmId received from constructor
        dismissIntent.putExtra("alarmIdKey", mAlarmId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(mContext,
                mAlarmId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Display Alarm Time in notification
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());

        // Get snoozeLength
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String snoozeLengthStr = sharedPref.getString(KEY_SNOOZE_LENGTH, "10");

        /* Set default value to 10
         * Add null check to avoid warning
         * and npe later
         */
        int snoozeLengthInt = 10;
        if (snoozeLengthStr != null)
            snoozeLengthInt = Integer.parseInt(snoozeLengthStr);


        // Add current time to snoozeLength(Min)* millis
        long snoozeTargetTime = System.currentTimeMillis() + snoozeLengthInt * 60000;
        String formattedTime = sdf.format(snoozeTargetTime);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_task)
                .setContentTitle(task.getTitle())
                .setContentText(task.getDescription()+"\n" + formattedTime)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .addAction(R.drawable.ic_task, "Dismiss",
                        dismissPendingIntent);

        /* Build notification with id as alarmID
         * So it can be cancelled by Receiver using alarmId
         *
         */
        mNotifyManager.notify(mAlarmId, builder.build());
    }

    // Use silenceTimeout to show actual alarmId instead of current time
    public void deliverMissedNotification(long alarmTime) {
        mNotifyManager = (NotificationManager) MyApplication.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Display Alarm Time in notification
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        // Set formattedTime
        String formattedTime = sdf.format(alarmTime);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                mContext, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_task)
                .setContentTitle("Sveglia")
                .setContentText("Sveglia persa: " + formattedTime)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        // Use alarmId just to create a unique notification
        mNotifyManager.notify(mAlarmId, builder.build());
    }
}