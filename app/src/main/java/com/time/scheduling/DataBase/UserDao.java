package com.time.scheduling.DataBase;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM user_table")
    void deleteAllUsers();

    @Query("DELETE FROM user_table WHERE id = :ItemId")
    void deleteUserById(int ItemId);

    @Query("SELECT * FROM user_table ORDER BY id DESC")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM user_table WHERE id = :ItemId LIMIT 1")
    User getUserById(int ItemId);

    @Query("SELECT * FROM user_table WHERE userName LIKE :name")
    User getUserByName(String name);

}
