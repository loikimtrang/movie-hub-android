package com.movie_hub.android.data.model.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity(tableName = "user"
//        ,foreignKeys = @ForeignKey(entity = OrderEntity.class,parentColumns = "id",childColumns = "user_id",onDelete = CASCADE)
)
public class UserEntity {
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    private long userId;

}
