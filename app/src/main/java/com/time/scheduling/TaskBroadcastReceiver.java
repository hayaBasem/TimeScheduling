package com.time.scheduling;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.Objects;

public class TaskBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_DISMISS = BuildConfig.APPLICATION_ID + ".ACTION_DISMISS";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Intent serviceIntent = new Intent(context, TaskService.class);
            //ContextCompat.startForegroundService(context, serviceIntent);

        } else if (Objects.equals(intent.getAction(), ACTION_DISMISS)) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            int alarmId = intent.getIntExtra("alarmIdKey", -1);

            Intent alarmCancelIntent = new Intent(context, TaskService.class);
            PendingIntent pendingIntent;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pendingIntent = PendingIntent.getForegroundService(context,
                        alarmId, alarmCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getService(context, alarmId, alarmCancelIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
            alarmManager.cancel(pendingIntent);

            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.cancel(alarmId);

        }
    }
}
