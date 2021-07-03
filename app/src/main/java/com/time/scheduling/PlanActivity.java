package com.time.scheduling;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.time.scheduling.DataBase.Task;
import com.time.scheduling.DataBase.TaskViewModel;
import com.time.scheduling.recycler.PlanViewAdapter;

import java.util.List;

public class PlanActivity extends AppCompatActivity {

    private TaskViewModel taskViewModel;

    private RecyclerView mRecyclerView;
    private PlanViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
        mRecyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new PlanViewAdapter(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        taskViewModel.getAllTasksByTime().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasksList) {
                mAdapter.submitList(tasksList);
            }
        });

    }
}