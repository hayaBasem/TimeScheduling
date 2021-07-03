package com.time.scheduling;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.PermissionChecker;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.time.scheduling.DataBase.Task;
import com.time.scheduling.DataBase.TaskViewModel;
import com.time.scheduling.DataBase.Utile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.time.scheduling.TaskTriggerActivity.getPendingIntent;

public class AddEditTaskActivity extends AppCompatActivity {


    private Uri imageuri;
    private CircleImageView userImage;
    private EditText title;
    private EditText description;
    private TextView task_time;
    private TextView task_date;
    private ImageView image_when_empty;
    private TextView value_priority;
    private ImageView priority_when_empty;
    private AppCompatButton save;
    private TextInputLayout title_layout;
    private long time;
    private int priority = 1;
    private Calendar c;

    private TaskViewModel taskViewModel;
    private int taskId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        taskId = getIntent().getIntExtra("task_id", -1);

        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        userImage = findViewById(R.id.user_image);
        title = findViewById(R.id.edit_text_title);
        description = findViewById(R.id.edit_text_description);
        task_time = findViewById(R.id.task_time);
        task_date = findViewById(R.id.task_date);
        image_when_empty = findViewById(R.id.image_when_empty);
        value_priority = findViewById(R.id.value_priority);
        priority_when_empty = findViewById(R.id.priority_when_empty);
        save = findViewById(R.id.save);
        title_layout = findViewById(R.id.title_layout);
        image_when_empty.setVisibility(View.VISIBLE);
        priority_when_empty.setVisibility(View.VISIBLE);
        task_time.setVisibility(View.INVISIBLE);
        task_date.setVisibility(View.INVISIBLE);
        value_priority.setVisibility(View.INVISIBLE);

        if(taskId != -1){
            Task task = taskViewModel.getTaskById(taskId);
            title.setText(task.getTitle());
            description.setText(task.getDescription());
            if(!task.getImage().equals("")){
                byte[] decodedString = Base64.decode(task.getImage().getBytes(), Base64.DEFAULT);
                userImage.setImageBitmap(Utile.getImage(decodedString));
            }
            priority_when_empty.setVisibility(View.GONE);
            value_priority.setVisibility(View.VISIBLE);
            value_priority.setText(task.getPriority()+"");
            priority = task.getPriority();
            image_when_empty.setVisibility(View.GONE);
            task_time.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                    Locale.getDefault());
            String formattedTime = sdf.format(task.getTime());
            task_time.setText(formattedTime);
            time = task.getTime();
            task_date.setVisibility(View.VISIBLE);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            if(month < 10 &&  day < 10){
                task_date.setText("0"+(month+1)+"/0"+day+"/"+year);
            }else if (month < 10){
                task_date.setText("0"+(month+1)+"/"+day+"/"+year);
            }else if(day < 10){
                task_date.setText((month+1)+"/0"+day+"/"+year);
            }else {
                task_date.setText((month+1)+"/"+day+"/"+year);
            }
            save.setText("Edit");
        }

        image_when_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime();
            }
        });
        task_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {pickTime();
            }
        });
        task_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime();
            }
        });
        value_priority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pick_priority();
            }
        });
        priority_when_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pick_priority();
            }
        });
        userImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(PermissionChecker.checkSelfPermission(AddEditTaskActivity.this,READ_EXTERNAL_STORAGE)!=
                        PermissionChecker.PERMISSION_GRANTED || PermissionChecker.checkSelfPermission(AddEditTaskActivity.this,WRITE_EXTERNAL_STORAGE)!=
                        PermissionChecker.PERMISSION_GRANTED)
                {
                    //asking for permisssions
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 401);
                }else {
                    //permission is already there
                    pick_image();
                }
            }
        });

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                title.setError(null);
                title_layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title.getText().toString().trim().equals("")){
                    title.setError("You must enter your Title");
                    title_layout.setError("You must enter your Title");
                    title_layout.setErrorIconDrawable(null);
                    return;
                }
                if(title.getText().toString().trim().length()<3){
                    title.setError("You must enter your full name");
                    title_layout.setError("You must enter your full name");
                    title_layout.setErrorIconDrawable(null);
                    return;
                }
                if(task_time.getText().toString().trim().equals("") || time == 0){
                    Toast.makeText(AddEditTaskActivity.this,"You must enter your Time and Date of Task",Toast.LENGTH_LONG).show();
                    return;
                }
                if(value_priority.getText().toString().trim().equals("")){
                    Toast.makeText(AddEditTaskActivity.this,"You must enter your Priority of Task",Toast.LENGTH_LONG).show();
                    return;
                }
                Task task = new Task();
                task.setEnabled(true);
                task.setTitle(title.getText().toString());
                task.setDescription(description.getText().toString());
                task.setTime(time);
                task.setPriority(priority);
                InputStream imageStream;
                Bitmap selectedImage;
                if(imageuri != null){
                    try {
                        imageStream = AddEditTaskActivity.this.getContentResolver().openInputStream(imageuri);
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        byte[] i = Utile.getbyte(selectedImage);
                        task.setImage(Base64.encodeToString(i, Base64.DEFAULT));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else {
                    task.setImage("");
                }
                if (c.before(Calendar.getInstance())) {
                    if (System.currentTimeMillis() > c.getTimeInMillis()){
                        c.add(Calendar.YEAR, 1);
                        task.setTime(c.getTimeInMillis());
                    }
                }
                AlarmManager alarmManager = (AlarmManager) AddEditTaskActivity.this.getSystemService(Context.ALARM_SERVICE);
                AlarmManager.AlarmClockInfo alarmClockInfo =
                        new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), null);

                if(taskId != -1){
                    task.setId(taskId);
                    taskViewModel.update(task);
                    alarmManager.setAlarmClock(alarmClockInfo, getPendingIntent(taskId));
                }else {
                    taskViewModel.insert(task);
                    alarmManager.setAlarmClock(alarmClockInfo, getPendingIntent(taskViewModel.getTaskByTitle(task.getTitle()).getId()));
                }


                onBackPressed();
            }
        });
    }

    private void pickTime(){
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);

                image_when_empty.setVisibility(View.GONE);
                task_time.setVisibility(View.VISIBLE);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa",
                        Locale.getDefault());
                String formattedTime = sdf.format(c.getTimeInMillis());
                task_time.setText(formattedTime);
                new DatePickerDialog(AddEditTaskActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                if(month < 10 &&  day < 10){
                                    task_date.setText("0"+(month+1)+"/0"+datePicker.getDayOfMonth()+"/"+year);
                                }else if (month < 10){
                                    task_date.setText("0"+(month+1)+"/"+datePicker.getDayOfMonth()+"/"+year);
                                }else if(day < 10){
                                    task_date.setText((month+1)+"/0"+datePicker.getDayOfMonth()+"/"+year);
                                }else {
                                    task_date.setText((month+1)+"/"+datePicker.getDayOfMonth()+"/"+year);
                                }
                                c.set(Calendar.DAY_OF_MONTH,day);
                                c.set(Calendar.MONTH,month);
                                c.set(Calendar.YEAR,year);
                                time = c.getTimeInMillis();
                                task_date.setVisibility(View.VISIBLE);
                            }
                        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show();
            }
        };
        Calendar currentTime = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                timeSetListener,
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(this));
        timePickerDialog.show();
    }

    private void pick_image() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), 101);
    }
    private void pick_priority() {
        NumberPicker numberPicker = new NumberPicker(AddEditTaskActivity.this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);
        if(priority != 1) numberPicker.setValue(priority);
        new AlertDialog.Builder(AddEditTaskActivity.this)
                .setTitle("Priority")
                .setView(numberPicker)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        priority_when_empty.setVisibility(View.GONE);
                        value_priority.setVisibility(View.VISIBLE);
                        value_priority.setText(numberPicker.getValue()+"");
                        priority = numberPicker.getValue();
                        dialog.cancel();
                    }
                }).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == RESULT_OK) {
                imageuri=   data.getData();//this the file url
                userImage.setImageURI(imageuri);
            }
        }
    }
}