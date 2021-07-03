package com.time.scheduling.DataBase;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class TaskRepository {
    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private LiveData<List<Task>> allTasksByTime;
    public TaskRepository(Application application) {
        MYDatabase database = MYDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        allTasksByTime = taskDao.getAllTasksByTime();
    }

    public void insert(Task task) {
        new TaskRepository.InsertTaskAsyncTask(taskDao).execute(task);
    }
    public void update(Task task) {
        new TaskRepository.UpdateTaskAsyncTask(taskDao).execute(task);
    }
    public void delete(Task task) {
        new TaskRepository.DeleteTaskAsyncTask(taskDao).execute(task);
    }
    public void deleteAllTasks() {
        new TaskRepository.DeleteAllTasksAsyncTask(taskDao).execute();
    }
    public void deleteTaskById(int id) {
        new TaskRepository.DeleteByIdTaskAsyncTask(taskDao).execute(id);
    }
    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }
    public LiveData<List<Task>> getAllTasksByTime() {
        return allTasksByTime;
    }
    public LiveData<List<Task>> getAllTasksEnabled(){
        try {
            return new GetAllTasksEnabledLiveDataAsyncTask(taskDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public LiveData<List<Task>> getAllTasksNotEnabled(){
        try {
            return new GetAllTasksNotEnabledrLiveDataAsyncTask(taskDao).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Task getTaskById(int id){
        try {
            return new GetTaskByIdAsyncTask(taskDao).execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Task getTaskByTitle(String title){
        try {
            return new GetTaskByTitleAsyncTask(taskDao).execute(title).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }



    private class InsertTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        public InsertTaskAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.insert(tasks[0]);
            return null;
        }
    }

    private class UpdateTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        public UpdateTaskAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.update(tasks[0]);
            return null;
        }
    }

    private class DeleteTaskAsyncTask extends AsyncTask<Task, Void, Void> {
        private TaskDao taskDao;
        public DeleteTaskAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Task... tasks) {
            taskDao.delete(tasks[0]);
            return null;
        }
    }

    private class DeleteAllTasksAsyncTask extends AsyncTask<Void, Void, Void> {
        private TaskDao taskDao;
        public DeleteAllTasksAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            taskDao.deleteAllTasks();
            return null;
        }
    }

    private class GetAllTasksEnabledLiveDataAsyncTask extends AsyncTask<Void, Void, LiveData<List<Task>>>{
        private TaskDao taskDao;
        public GetAllTasksEnabledLiveDataAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected LiveData<List<Task>> doInBackground(Void... voids) {
            return taskDao.getAllTasksEnabled();
        }
    }
    private class GetAllTasksNotEnabledrLiveDataAsyncTask extends AsyncTask<Void, Void, LiveData<List<Task>>>{
        private TaskDao taskDao;
        public GetAllTasksNotEnabledrLiveDataAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected LiveData<List<Task>> doInBackground(Void... voids) {
            return taskDao.getAllTasksNotEnabled();
        }
    }

    private class GetTaskByIdAsyncTask extends AsyncTask<Integer, Void, Task> {
        private TaskDao taskDao;
        public GetTaskByIdAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected Task doInBackground(Integer... integers) {
            return taskDao.getTaskById(integers[0]);
        }
    }

    private class GetTaskByTitleAsyncTask extends AsyncTask<String, Void, Task> {
        private TaskDao taskDao;
        public GetTaskByTitleAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected Task doInBackground(String... strings) {
            return taskDao.getTaskByTitle(strings[0]);
        }
    }

    private class DeleteByIdTaskAsyncTask extends AsyncTask<Integer, Void, Void> {
        private TaskDao taskDao;
        public DeleteByIdTaskAsyncTask(TaskDao taskDao) {
            this.taskDao = taskDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            taskDao.deleteTaskById(integers[0]);
            return null;
        }
    }
}
