package com.time.scheduling.DataBase;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Task.class,User.class}, version = 7)
public abstract class MYDatabase extends RoomDatabase {

    private static MYDatabase instance;

    public abstract UserDao userDao();
    public abstract TaskDao taskDao();

    public static synchronized MYDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MYDatabase.class, "note_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static Callback roomCallback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private UserDao userDao;
        private TaskDao taskDao;
        private PopulateDbAsyncTask(MYDatabase db) {
            userDao = db.userDao();
            taskDao = db.taskDao();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
