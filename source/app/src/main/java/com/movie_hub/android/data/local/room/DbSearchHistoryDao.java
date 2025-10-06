package com.movie_hub.android.data.local.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.movie_hub.android.data.model.room.SearchHistoryEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface DbSearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(SearchHistoryEntity entity);

    @Query("SELECT * FROM search_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT 10")
    Single<List<SearchHistoryEntity>> getRecentHistory(long userId);

    @Query("DELETE FROM search_history WHERE user_id = :userId")
    Completable clearByUser(long userId);

    @Query("DELETE FROM search_history WHERE id IN (SELECT id FROM search_history WHERE user_id = :userId ORDER BY created_at ASC LIMIT 1)")
    Completable deleteOldest(long userId);

    @Query("SELECT COUNT(*) FROM search_history WHERE user_id = :userId")
    Single<Integer> countByUser(long userId);


    @Query("DELETE FROM search_history WHERE user_id = :userId AND keyword = :keyword")
    Completable deleteDuplicate(long userId, String keyword);
}


