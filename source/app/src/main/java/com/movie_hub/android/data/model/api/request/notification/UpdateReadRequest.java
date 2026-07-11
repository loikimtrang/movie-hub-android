package com.movie_hub.android.data.model.api.request.notification;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UpdateReadRequest {
    List<Long> ids = new ArrayList<>();
}
