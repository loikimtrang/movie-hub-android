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
    Completable insert(UserEntity UserEntity);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<UserEntity> userEntities);

    @Query("SELECT * FROM `user`")
    Single<List<UserEntity>> loadAll();

    @Query("SELECT * FROM `user` ORDER BY user_id")
    LiveData<List<UserEntity>> loadAllToLiveData();

    @Query("SELECT * FROM `user` WHERE user_id=:id")
    Single<UserEntity> findById(long id);

    @Delete
    Completable delete(UserEntity UserEntity);

}
