package com.time.scheduling.DataBase;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private UserRepository repository;
    private LiveData<List<User>> allUsers;
    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        allUsers = repository.getAllUsers();
    }
    public void insert(User user) {
        repository.insert(user);
    }
    public void update(User user) {
        repository.update(user);
    }
    public void delete(User user) {
        repository.delete(user);
    }
    public void deleteUserById(int userid) {
        repository.deleteUserById(userid);
    }
    public void deleteAllNotes() {
        repository.deleteAllNotes();
    }
    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }
    public User getUserById(int id) {
        return repository.getUserById(id);
    }
    public User getUserByName(String name) {
        return repository.getUserByName(name);
    }
}
