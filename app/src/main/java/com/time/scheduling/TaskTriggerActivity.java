package com.time.scheduling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ncorti.slidetoact.SlideToActView;
import com.time.scheduling.DataBase.Task;
import com.time.scheduling.DataBase.TaskRepository;
import com.time.scheduling.DataBase.TaskViewModel;
import com.time.scheduling.DataBase.UserViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import static com.time.scheduling.TaskService.ACTION_DISMISS;
import static com.time.scheduling.TaskService.ACTION_DO_NOTHING;
import static com.time.scheduling.TaskService.ACTION_MUTE;
import static com.time.scheduling.TaskService.ACTION_SNOOZE;

public class TaskTriggerActivity extends AppCompatActivity {


    private TextView tvTaskTime, tvTaskTitle, tvTemperature, tvWeatherType;
    private ImageView ivWeatherIcon;

    // vars
    private static final String TAG = "TaskTriggerActivity";
    private boolean isSnoozed = false;
    private Handler handler;
    private Runnable silenceRunnable;
    private Task task;
    private SharedPreferences sharedPref;
    private String actionBtnPref;
    private PowerManager.WakeLock wakeLock;
    private TaskViewModel taskViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskViewModel = ViewModelProviders.of(TaskTriggerActivity.this).get(TaskViewModel.class);
        setContentView(R.layout.activity_task_trigger);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "TiemScheduling::TaskTriggerWakeLock");

        wakeLock.acquire(900000);

        turnOnScreen();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(PowerBtnReceiver, filter);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        tvTaskTime = findViewById(R.id.trigger_task_time);
        tvTaskTitle = findViewById(R.id.trigger_task_title);
        tvTemperature = findViewById(R.id.temperature);
        tvWeatherType = findViewById(R.id.weather_type);
        ivWeatherIcon = findViewById(R.id.weather_icon);
        SlideToActView btnDismissTask = findViewById(R.id.btn_dismiss_task);
        SlideToActView btnSnoozeTask = findViewById(R.id.btn_snooze_task);

        Intent intent = getIntent();

        int alarmId = -1;
        int taskId = -1;
        if (intent.hasExtra("alarmIdKey"))
            alarmId = intent.getIntExtra("alarmIdKey", -1);
        if (intent.hasExtra("taskId"))
            taskId = intent.getIntExtra("taskId", -1);

        buildDisplayInfo(taskId);

        btnDismissTask.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                stopTaskService();
            }
        });

        btnSnoozeTask.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(@NonNull SlideToActView slideToActView) {
                snoozeTask();
            }
        });

        silenceTimeout(alarmId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;

        unregisterReceiver(PowerBtnReceiver);
    }

    private void buildDisplayInfo(int taskId) {


        final int finalTaskId = taskId;
        MyApplication.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                task = taskViewModel.getTaskById(finalTaskId);

                try {
                    displayInfo(task);
                }
                catch (NullPointerException e) {
                    try {
                        Calendar todayCal = Calendar.getInstance();
                        int dayToday = todayCal.get(Calendar.DAY_OF_WEEK);

                        int taskId = finalTaskId - dayToday;

                        Task parentEntity = taskViewModel.getTaskById(taskId);

                        displayInfo(parentEntity);
                    } catch (NullPointerException e1) {
                        displaySnoozedInfo(task);
                        isSnoozed = true;
                        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotifyManager.cancelAll();
                    }
                }
            }
        });
    }


    public void displayInfo(final Task task) {
        final long taskTimeInMillis = task.getTime();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                        Locale.getDefault());
                String formattedTime = sdf.format(taskTimeInMillis);
                tvTaskTime.setText(formattedTime);
                if (task.getTitle().trim().equals("Nome Sveglia"))
                    tvTaskTitle.setVisibility(View.GONE);
                else{
                    tvTaskTitle.setText(task.getTitle());
                    tvTemperature.setText(task.getDescription());
                }

            }
        });
    }

    private void displaySnoozedInfo(Task task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                        Locale.getDefault());
                String formattedTime = sdf.format(System.currentTimeMillis());
                tvTaskTime.setText(formattedTime);

                tvTaskTitle.setText(task.getTitle());
                tvTemperature.setText(task.getDescription());
            }
        });
    }

    public void silenceTimeout(final int alarmId) {
        final String KEY_SILENCE_TIMEOUT = "silenceTimeout";

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String silenceTimeStr = sharedPref.getString(KEY_SILENCE_TIMEOUT, "0");

        int silenceTimeoutInt = 0;
        if (silenceTimeStr != null)
            silenceTimeoutInt = Integer.parseInt(silenceTimeStr);

        if (silenceTimeoutInt == 0)
            return;

        handler = new Handler(getMainLooper());
        silenceRunnable = new Runnable() {
            @Override
            public void run() {

                NotificationHelper nh = new NotificationHelper(getApplicationContext(), alarmId);

                if (isSnoozed) {
                    nh.deliverMissedNotification(
                            System.currentTimeMillis() - (Long.parseLong(silenceTimeStr) * 60000));
                } else
                    nh.deliverMissedNotification(task.getTime());

                stopTaskService();
            }
        };
        handler.postDelayed(silenceRunnable, silenceTimeoutInt * 60000); // x Minutes * millis
    }

    public void stopTaskService() {
        wakeLock.release();
        Intent intent = new Intent(TaskTriggerActivity.this, TaskService.class);
        stopService(intent);
        task.setEnabled(false);
        taskViewModel.update(task);

        if (handler != null && silenceRunnable != null)
            handler.removeCallbacks(silenceRunnable);
        finish();
    }

    public void snoozeTask() {

        final String KEY_SNOOZE_LENGTH = "snoozeLength";

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        int alarmId = new Random().nextInt(Integer.MAX_VALUE);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String snoozeLengthStr = sharedPref.getString(KEY_SNOOZE_LENGTH, "10");

        int snoozeLengthInt = 10;
        if (snoozeLengthStr != null)
            snoozeLengthInt = Integer.parseInt(snoozeLengthStr);

        // Add snooze length
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, snoozeLengthInt);
        c.set(Calendar.SECOND, 0);

        AlarmManager.AlarmClockInfo alarmClockInfo =
                new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), null);

        alarmManager.setAlarmClock(alarmClockInfo, getPendingIntent(alarmId));
        NotificationHelper notificationHelper = new NotificationHelper(this, alarmId);
        notificationHelper.deliverPersistentNotification(task);

        stopTaskService();
    }
    public static PendingIntent getPendingIntent(int taskId) {

        Intent intent = new Intent(MyApplication.getContext(), TaskService.class);

        intent.putExtra("taskId", taskId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(MyApplication.getContext(), taskId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        } else
            return PendingIntent.getService(MyApplication.getContext(), taskId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private void turnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        } else {
            final Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            actionBtnPref = sharedPref.getString("volume_btn_action", ACTION_DO_NOTHING);
            if (actionBtnPref != null)
                actionBtnHandler(actionBtnPref);
        }
        return super.onKeyDown(keyCode, event);
    }

    /* This method:
     * Receives Power button press (Screen off event)
     */
    private final BroadcastReceiver PowerBtnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {

                    // Get power key pref
                    actionBtnPref = sharedPref.getString("power_btn_action", ACTION_DO_NOTHING);
                    if (actionBtnPref != null)
                        actionBtnHandler(actionBtnPref);
                }
            }
        }
    };

    private void actionBtnHandler(String action) {
        switch (action) {
            case ACTION_MUTE:
                sendBroadcast(new Intent().setAction(ACTION_MUTE));
                break;
            case ACTION_DISMISS:
                stopTaskService();
                break;
            case ACTION_SNOOZE:
                snoozeTask();
                break;
        }
    }
}