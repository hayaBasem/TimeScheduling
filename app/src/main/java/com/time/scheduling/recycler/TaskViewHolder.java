package com.time.scheduling.recycler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.time.scheduling.AddEditTaskActivity;
import com.time.scheduling.DataBase.Task;
import com.time.scheduling.DataBase.TaskRepository;
import com.time.scheduling.DataBase.TaskViewModel;
import com.time.scheduling.DataBase.Utile;
import com.time.scheduling.ListActivity;
import com.time.scheduling.MyApplication;
import com.time.scheduling.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.time.scheduling.ListActivity.ADD_NOTE_REQUEST;
import static com.time.scheduling.TaskTriggerActivity.getPendingIntent;

public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    // vars
    private static final String TAG = "PK:AlarmRecViewAdapter";
    private Task currentTask;
    private String formattedTime;
    private Context context;

    // UI Components
    private TextView task_time;
    private EditText task_title;
    private EditText task_description;
    private EditText task_priority;
    private ConstraintLayout repeatDaysLayout;
    private SwitchCompat switchTaskEnabled;
    private ImageButton task_show_repeat;
    private ImageButton task_hide_repeat;
    private TaskViewModel taskViewModel;
    private LinearLayout liner_title;
    private LinearLayout liner_description;
    private LinearLayout liner_priority;

    private CircleImageView user_image;
    private Activity activity;

    public TaskViewHolder(@NonNull View itemView, Activity mActivity) {
        super(itemView);
        activity = mActivity;
        // Get context
        context = itemView.getContext();
        taskViewModel = ViewModelProviders.of((ListActivity)mActivity).get(TaskViewModel.class);
        // Find views
        task_time = itemView.findViewById(R.id.task_time);

        task_title = itemView.findViewById(R.id.task_title);
        task_description = itemView.findViewById(R.id.text_view_description);
        task_priority = itemView.findViewById(R.id.text_view_priority);
        switchTaskEnabled = itemView.findViewById(R.id.switchTaskEnabled);
        ImageButton task_delete = itemView.findViewById(R.id.task_delete);
        task_show_repeat = itemView.findViewById(R.id.task_show_repeat);
        task_hide_repeat = itemView.findViewById(R.id.task_hide_repeat);
        repeatDaysLayout = itemView.findViewById(R.id.repeat_days_layout);
        liner_title = itemView.findViewById(R.id.liner_title);
        liner_description = itemView.findViewById(R.id.liner_description);
        liner_priority = itemView.findViewById(R.id.liner_priority);
        user_image = itemView.findViewById(R.id.user_image);
        // Set onClickListeners
        switchTaskEnabled.setOnClickListener(this);
        task_delete.setOnClickListener(this);
        task_show_repeat.setOnClickListener(this);
        task_hide_repeat.setOnClickListener(this);
        task_title.setOnClickListener(this);

        repeatDaysLayout.setVisibility(View.GONE);
    }

    public void bindTo(Task currentItem) {
        this.currentTask = currentItem;

        // Get time from milliSeconds to format: 08:30 PM
        long alarmTimeInMillis = currentTask.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                Locale.getDefault());
        formattedTime = sdf.format(alarmTimeInMillis);


        task_time.setText(formattedTime);
        switchTaskEnabled.setChecked(currentTask.isEnabled());
        task_title.setText(currentTask.getTitle());
        task_description.setText(currentTask.getDescription());
        task_priority.setText(currentTask.getPriority()+"");

        if(!currentItem.getImage().equals("")){
            byte[] decodedString = Base64.decode(currentItem.getImage().getBytes(), Base64.DEFAULT);
            user_image.setImageBitmap(Utile.getImage(decodedString));
        }

        task_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((ListActivity)activity, AddEditTaskActivity.class);
                intent.putExtra("task_id",currentItem.getId());
                ((ListActivity)activity).startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });
        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.switchTaskEnabled:
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (!switchTaskEnabled.isChecked()) {
                    currentTask.setEnabled(false);
                    alarmManager.cancel(getPendingIntent(currentTask.getId()));
                } else {
                    while (System.currentTimeMillis() > currentTask.getTime()){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(currentTask.getTime());
                        calendar.add(Calendar.DATE, 1);
                        currentTask.setTime(calendar.getTimeInMillis());
                    }
                    AlarmManager.AlarmClockInfo alarmClockInfo =
                            new AlarmManager.AlarmClockInfo(currentTask.getTime(), null);
                    alarmManager.setAlarmClock(alarmClockInfo, getPendingIntent(currentTask.getId()));
                    currentTask.setEnabled(true);
                }
                taskViewModel.update(currentTask);
                break;
            case R.id.task_delete:
                // pass alarm id and true to delete: if false then just disable alarm
                taskViewModel.delete(currentTask);
                break;
            case R.id.task_show_repeat:
                repeatDaysLayout.setVisibility(View.VISIBLE);
                task_show_repeat.setVisibility(View.INVISIBLE);
                task_hide_repeat.setVisibility(View.VISIBLE);
                liner_title.setVisibility(View.VISIBLE);
                liner_description.setVisibility(View.VISIBLE);
                liner_priority.setVisibility(View.VISIBLE);
                break;
            case R.id.task_hide_repeat:
                repeatDaysLayout.setVisibility(View.GONE);
                task_show_repeat.setVisibility(View.VISIBLE);
                task_hide_repeat.setVisibility(View.INVISIBLE);
                liner_title.setVisibility(View.INVISIBLE);
                liner_description.setVisibility(View.INVISIBLE);
                liner_priority.setVisibility(View.INVISIBLE);
                break;
        }
    }

}
