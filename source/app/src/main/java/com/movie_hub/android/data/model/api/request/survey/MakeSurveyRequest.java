package com.movie_hub.android.data.model.api.request.survey;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MakeSurveyRequest {
    List<Long> movieIds = new ArrayList<>();
}
