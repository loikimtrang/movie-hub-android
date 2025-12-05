package com.movie_hub.android.data.model.api.request.movie.filter;

import com.movie_hub.android.constant.Constants;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class AgeRatingRequest {
    private int type;
    private String label;
    private boolean isSelect;

    public AgeRatingRequest() {
    }

    public AgeRatingRequest(int type, String label) {
        this.type = type;
        this.label = label;
    }
    public static List<AgeRatingRequest> getAll() {
        List<AgeRatingRequest> list = new ArrayList<>();
        list.add(new AgeRatingRequest(Constants.AGE_RATING_P, "P"));
        list.add(new AgeRatingRequest(Constants.AGE_RATING_K, "K"));
        list.add(new AgeRatingRequest(Constants.AGE_RATING_T13, "T13"));
        list.add(new AgeRatingRequest(Constants.AGE_RATING_T16, "T16"));
        list.add(new AgeRatingRequest(Constants.AGE_RATING_T18, "T18"));
        return list;
    }

}
