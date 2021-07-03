package com.time.scheduling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.time.scheduling.DataBase.Task;
import com.time.scheduling.DataBase.TaskViewModel;
import com.time.scheduling.DataBase.User;
import com.time.scheduling.DataBase.UserViewModel;
import com.time.scheduling.recycler.TaskViewAdapter;

import java.util.List;

public class ListActivity extends AppCompatActivity {


    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;

    private TaskViewModel taskViewModel;
    private ImageView noTasksImage;
    private TextView noTasksText;

    private RecyclerView mRecyclerView;
    private TaskViewAdapter mAdapter;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Intent intent = getIntent();
        if (intent.hasExtra("userId"))
            userId = intent.getIntExtra("userId", -1);

        noTasksImage = findViewById(R.id.image_when_empty);
        noTasksText = findViewById(R.id.text_when_empty);
        mRecyclerView = findViewById(R.id.recycler_view);

        FloatingActionButton buttonAddTask = findViewById(R.id.button_add_task);
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, AddEditTaskActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });


        FloatingActionButton buttonTableTasks = findViewById(R.id.button_table_note);
        buttonTableTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListActivity.this, PlanActivity.class);
                startActivity(intent);
            }
        });


        NotificationHelper notificationHelper = new NotificationHelper(ListActivity.this, -1);
        notificationHelper.createNotificationChannel();

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(ListActivity.this).setMessage("Are you sure you want to sign out ?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                new AlertDialog.Builder(ListActivity.this).setMessage("You will lose all your tasks?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        taskViewModel.deleteAllTasks();
                        if(userId != -1){
                            UserViewModel userViewModel = ViewModelProviders.of(ListActivity.this).get(UserViewModel.class);
                            User user = userViewModel.getUserById(userId);
                            user.setLogin(false);
                            userViewModel.update(user);
                        }
                        backManual();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void backManual(){
        super.onBackPressed();
    }

    private void initRecyclerView() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new TaskViewAdapter(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initRecyclerView();
        initViewModel();
        itemTouchHelper();
    }

    private void initViewModel() {
        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        taskViewModel.getAllTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasksList) {
                mAdapter.submitList(tasksList);
                if (tasksList.size() == 0) {
                    noTasksImage.setVisibility(View.VISIBLE);
                    noTasksText.setVisibility(View.VISIBLE);
                } else {
                    noTasksImage.setVisibility(View.GONE);
                    noTasksText.setVisibility(View.GONE);
                }
            }
        });
    }
    private void itemTouchHelper() {

        // Helper method for Drag to del room item
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // This also cancels the alarm
                int adapterPos = viewHolder.getAdapterPosition();
                Task currentTask = mAdapter.getTaskView(adapterPos);
                taskViewModel.delete(currentTask);
                Snackbar.make(viewHolder.itemView, "Task Removed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        helper.attachToRecyclerView(mRecyclerView);

    }
}