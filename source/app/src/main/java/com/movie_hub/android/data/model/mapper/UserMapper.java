package com.movie_hub.android.data.model.mapper;

import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.room.UserEntity;

public class UserMapper {
    public static UserEntity fromResponse(UserResponse userResponse) {
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
        return entity;
    }
    public static UserResponse toResponse(UserEntity entity) {
        if (entity == null) return null;

        UserResponse response = new UserResponse();
        response.setId(entity.getUserId());
        response.setAvatarPath(entity.getAvatarPath());
        response.setCreatedDate(entity.getCreatedDate());
        response.setEmail(entity.getEmail());
        response.setFullName(entity.getFullName());
        response.setGender(entity.getGender());
        response.setModifiedDate(entity.getModifiedDate());
        response.setPhone(entity.getPhone());
        response.setStatus(entity.getStatus());
        response.setUsername(entity.getUsername());
        response.setKind(entity.getKind());
        return response;
    }
}

