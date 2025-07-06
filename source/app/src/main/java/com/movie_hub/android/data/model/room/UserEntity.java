package com.movie_hub.android.data.model.room;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.movie_hub.android.data.model.api.response.user.UserResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity(tableName = "user")
public class UserEntity {

    @PrimaryKey
    @ColumnInfo(name = "user_id")
    private long userId;

    @ColumnInfo(name = "avatar_path")
    private String avatarPath;

    @ColumnInfo(name = "created_date")
    private String createdDate;

    private String email;

    @ColumnInfo(name = "full_name")
    private String fullName;

    private int gender;

    private int kind;

    @ColumnInfo(name = "modified_date")
    private String modifiedDate;

    private String phone;

    private int status;

    private String username;

}
