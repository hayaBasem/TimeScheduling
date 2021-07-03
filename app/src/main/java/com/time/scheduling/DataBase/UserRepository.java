package com.time.scheduling.DataBase;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserRepository {
    private UserDao userDao;
    private LiveData<List<User>> allUsers;
    public UserRepository(Application application) {
        MYDatabase database = MYDatabase.getInstance(application);
        userDao = database.userDao();
        allUsers = userDao.getAllUsers();
    }
    public void insert(User user) {
        new UserRepository.InsertUserAsyncTask(userDao).execute(user);
    }
    public void update(User user) {
        new UserRepository.UpdateUserAsyncTask(userDao).execute(user);
    }
    public void delete(User user) {
        new UserRepository.DeleteUserAsyncTask(userDao).execute(user);
    }
    public void deleteAllNotes() {
        new UserRepository.DeleteAllUsersAsyncTask(userDao).execute();
    }
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }
    public void deleteUserById(int userId) {
        new UserRepository.DeleteUserByIdUserAsyncTask(userDao).execute(userId);
    }
    public User getUserByName(String name){
        try {
            return new GetCustomerByNameAsyncTask(userDao).execute(name).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    public User getUserById(int id){
        try {
            return new GetCustomerByIDAsyncTask(userDao).execute(id).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;
        private InsertUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        @Override
        protected Void doInBackground(User... users) {
            userDao.insert(users[0]);
            return null;
        }
    }
    private static class UpdateUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;
        private UpdateUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        @Override
        protected Void doInBackground(User... users) {
            userDao.update(users[0]);
            return null;
        }
    }
    private static class DeleteUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;
        private DeleteUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        @Override
        protected Void doInBackground(User... users) {
            userDao.delete(users[0]);
            return null;
        }
    }
    private static class DeleteAllUsersAsyncTask extends AsyncTask<Void, Void, Void> {
        private UserDao userDao;
        private DeleteAllUsersAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            userDao.deleteAllUsers();
            return null;
        }
    }

    private class GetCustomerByNameAsyncTask extends AsyncTask<String, Void, User>{
        private UserDao userDao;
        public GetCustomerByNameAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected User doInBackground(String... strings) {
            return userDao.getUserByName(strings[0]);
        }
    }
    private class GetCustomerByIDAsyncTask extends AsyncTask<Integer, Void, User>{
        private UserDao userDao;
        public GetCustomerByIDAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected User doInBackground(Integer... ids) {
            return userDao.getUserById(ids[0]);
        }
    }

    private class DeleteUserByIdUserAsyncTask extends AsyncTask<Integer, Void, Void>{
        private UserDao userDao;
        public DeleteUserByIdUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            userDao.deleteUserById(integers[0]);
            return null;
        }
    }
}
