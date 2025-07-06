package com.movie_hub.android.data.local.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.movie_hub.android.data.model.room.UserEntity;

@Database(
        entities = {UserEntity.class},
        version = 1,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DbUserDao getUserDao();
}
