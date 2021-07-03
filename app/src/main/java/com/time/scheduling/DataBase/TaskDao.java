package com.time.scheduling.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM task_table")
    void deleteAllTasks();

    @Query("DELETE FROM task_table WHERE id = :ItemId")
    void deleteTaskById(int ItemId);

    @Query("SELECT * FROM task_table ORDER BY priority ASC")
    LiveData<List<Task>> getAllTasks();
    @Query("SELECT * FROM task_table ORDER BY time ASC")
    LiveData<List<Task>> getAllTasksByTime();

    @Query("SELECT * FROM task_table WHERE enabled ORDER BY priority ASC")
    LiveData<List<Task>> getAllTasksEnabled();

    @Query("SELECT * FROM task_table WHERE Not enabled ORDER BY priority ASC")
    LiveData<List<Task>> getAllTasksNotEnabled();

    @Query("SELECT * FROM task_table WHERE id = :ItemId LIMIT 1")
    Task getTaskById(int ItemId);

    @Query("SELECT * FROM task_table WHERE title LIKE :title ORDER BY id DESC")
    Task getTaskByTitle(String title);
}
