package com.movie_hub.android.data.local.room;

import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.room.UserEntity;

import javax.inject.Inject;

public class AppDbService implements RoomService {

    private final AppDatabase mAppDatabase;

    @Inject
    public AppDbService(AppDatabase appDatabase) {
        this.mAppDatabase = appDatabase;
    }


    @Override
    public DbUserDao userDao() {
        return mAppDatabase.getUserDao();
    }


    public void saveUserResponse(UserResponse userResponse) {
        if (userResponse == null) return;

        UserEntity entity = new UserEntity();
        entity.setUserId(userResponse.getId());
        entity.setAvatarPath(userResponse.getAvatarPath());
        entity.setCreatedDate(userResponse.getCreatedDate());
        entity.setEmail(userResponse.getEmail());
        entity.setFullName(userResponse.getFullName());
        entity.setGender(userResponse.getGender());
        entity.setModifiedDate(userResponse.getModifiedDate());
        entity.setPhone(userResponse.getPhone());
        entity.setStatus(userResponse.getStatus());
        entity.setUsername(userResponse.getUsername());
        entity.setKind(userResponse.getKind());

        mAppDatabase.getUserDao().insert(entity);
    }
}
