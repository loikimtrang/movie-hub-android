package com.movie_hub.android.data.local.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.movie_hub.android.data.model.room.UserEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DbUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(UserEntity user);

    @Query("SELECT * FROM `user` LIMIT 1")
    Single<UserEntity> getCurrentUser();

    @Query("DELETE FROM `user`")
    Completable clear();

}
