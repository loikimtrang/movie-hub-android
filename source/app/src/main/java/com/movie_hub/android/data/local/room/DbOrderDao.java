package com.movie_hub.android.data.local.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.movie_hub.android.data.model.room.OrderEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DbOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(OrderEntity OrderEntity);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAll(List<OrderEntity> userEntities);

    @Query("SELECT * FROM `order`")
    Single<List<OrderEntity>> loadAll();

    @Query("SELECT * FROM `order` ORDER BY id")
    LiveData<List<OrderEntity>> loadAllToLiveData();

    @Query("SELECT * FROM `order` WHERE id=:id")
    Single<OrderEntity> findById(long id);

    @Delete
    Completable delete(OrderEntity OrderEntity);

}
