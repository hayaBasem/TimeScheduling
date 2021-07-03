package com.time.scheduling.DataBase;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Task>> allTasksByTime;
    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        allTasksByTime = repository.getAllTasksByTime();
    }

    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    public void deleteAllTasks() {
        repository.deleteAllTasks();
    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    public LiveData<List<Task>> getAllTasksByTime() {
        return allTasksByTime;
    }

    public LiveData<List<Task>> getAllTasksEnabled(){return repository.getAllTasksEnabled();}
    public LiveData<List<Task>> getAllTasksNotEnabled(){return repository.getAllTasksNotEnabled();}
    public Task getTaskById(int id){return repository.getTaskById(id);}
    public Task getTaskByTitle(String title){return repository.getTaskByTitle(title);}


}