package com.movie_hub.android.data.model.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "search_history")
public class SearchHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "user_id")
    public long userId;

    @ColumnInfo(name = "keyword")
    public String keyword;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
