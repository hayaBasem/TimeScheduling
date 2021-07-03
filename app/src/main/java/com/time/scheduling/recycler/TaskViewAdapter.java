package com.time.scheduling.recycler;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.time.scheduling.DataBase.Task;
import com.time.scheduling.R;


public class TaskViewAdapter extends ListAdapter<Task, TaskViewHolder> {


    private Activity mActivity;

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.isEnabled() == newItem.isEnabled()
                    && oldItem.getTime() == newItem.getTime()
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getPriority() == newItem.getPriority();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.isEnabled() == newItem.isEnabled()
                    && oldItem.getTime() == newItem.getTime()
                    && oldItem.getDescription().equals(newItem.getDescription())
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getPriority() == newItem.getPriority();
        }
    };

    public TaskViewAdapter(Activity activity) {
        super(DIFF_CALLBACK);
        this.mActivity = activity;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.task_item, parent, false);
        return new TaskViewHolder(itemView,mActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull final TaskViewHolder holder, final int position) {

        final Task currentItem = getItem(position);
        holder.bindTo(currentItem);
    }


    public Task getTaskView(int position) {
        return getItem(position);
    }

}