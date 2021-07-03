package com.time.scheduling;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ServiceLifecycleDispatcher;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.time.scheduling.DataBase.Task;
import com.time.scheduling.DataBase.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

public class TaskService extends Service implements LifecycleOwner {

    private Vibrator v;
    private MediaPlayer player;
    private Handler handler;
    private Runnable crescendoRunnable;
    private SharedPreferences sharedPref;

    public static final String ACTION_MUTE = "com.time.scheduling.MUTE";
    public static final String ACTION_DO_NOTHING = "com.time.scheduling.DO_NOTHING";
    public static final String ACTION_DISMISS = "com.time.scheduling.DISMISS";
    public static final String ACTION_SNOOZE = "com.time.scheduling.SNOOZE";

    private TaskViewModel taskViewModel;
    ServiceLifecycleDispatcher lifecycleDispatcher;
    List<Task> taskList;

    @Override
    public void onCreate() {
        super.onCreate();
        lifecycleDispatcher = new ServiceLifecycleDispatcher(this);
        lifecycleDispatcher.onServicePreSuperOnCreate();
        taskList = new ArrayList<>();
        taskViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication()).create(TaskViewModel.class);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        lifecycleDispatcher.onServicePreSuperOnStart();
        taskViewModel.getAllTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                taskList = tasks;

            }
        });
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // Register mute action receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MUTE);
        registerReceiver(MuteActionReceiver, filter);

        int taskId = -1;
        if (intent != null && intent.hasExtra("taskId"))
            taskId = intent.getIntExtra("taskId", -1);

        startForeground(1, new NotificationHelper(this, -1).deliverNotification(taskViewModel.getTaskById(taskId)));


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Intent taskActivityIntent = new Intent(this, TaskTriggerActivity.class);
            taskActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            taskActivityIntent.putExtra("taskId", taskId);
            startActivity(taskActivityIntent);
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        playTask();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy();
        super.onDestroy();

        if (handler != null && crescendoRunnable != null)
            handler.removeCallbacks(crescendoRunnable);

        if (player != null) {
            player.release();
            player = null;
            v.cancel();
        }

        unregisterReceiver(MuteActionReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void playTask() {

        Uri defaultSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
        Uri taskSound = defaultSound;


        if (sharedPref != null && sharedPref.contains("ringtone")) {
            taskSound = Uri.parse(sharedPref.getString("ringtone", defaultSound.toString()));
        }

        player = new MediaPlayer();

        try {
            player.setDataSource(this, taskSound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .build();
            player.setLooping(true);
            player.setAudioAttributes(audioAttributes);
            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                startPlayerOnPrepared();
            }
        });
        vibrateTask();
    }

    public void startPlayerOnPrepared() {

        final float MAX_VOLUME = 1.0f;
        final String KEY_CRESCENDO_TIME = "crescendoTime";

        String crescendoTimeStr = sharedPref.getString(KEY_CRESCENDO_TIME, "0");


        float crescendoTime = 0;
        if (crescendoTimeStr != null)
            crescendoTime = Integer.parseInt(crescendoTimeStr);

        if (crescendoTime == 0) {
            player.start();
            return;
        }

        player.setVolume(0, 0);

        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        final float incrementPerSecond = 1 / crescendoTime;

        crescendoRunnable = new Runnable() {
            float currentVol = 0.0f;

            @Override
            public void run() {
                if (player == null) return;

                if (currentVol < MAX_VOLUME) {
                    currentVol = currentVol + incrementPerSecond;
                    player.setVolume(currentVol, currentVol);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        player.start();
        handler.post(crescendoRunnable);
    }

    @SuppressLint("MissingPermission")
    public void vibrateTask() {
        final String KEY_VIBRATE = "vibrateEnabled";

        boolean vibrationEnabled = sharedPref.getBoolean(KEY_VIBRATE, true);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrationEnabled) {
            long[] vibratePattern = new long[]{0, 500, 1000};
            VibrationEffect effect;

            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                effect = VibrationEffect.createWaveform(vibratePattern, 0);
                v.vibrate(effect);
            } else {
                v.vibrate(vibratePattern, 0);
            }
        }
    }

    private final BroadcastReceiver MuteActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_MUTE.equals(intent.getAction())) {
                muteTask();
            }
        }
    };

    @SuppressLint("MissingPermission")
    public void muteTask() {
        if (handler != null && crescendoRunnable != null) handler.removeCallbacks(crescendoRunnable);
        player.stop();
        v.cancel();
    }
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleDispatcher.getLifecycle();
    }
}