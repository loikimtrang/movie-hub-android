package com.movie_hub.android.data.model.api.request.movie.filter;

import android.content.Context;

import com.movie_hub.android.R;
import com.movie_hub.android.constant.Constants;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class TypeMovieRequest {
    private int type;
    private String label;
    private boolean isSelect;

    public TypeMovieRequest() {
    }

    public TypeMovieRequest(int type, String label) {
        this.type = type;
        this.label = label;
    }
    public static List<TypeMovieRequest> getAll(Context context) {
        List<TypeMovieRequest> list = new ArrayList<>();
        list.add(new TypeMovieRequest(Constants.TYPE_MOVIE_SINGLE, context.getString(R.string.label_movie)));
        list.add(new TypeMovieRequest(Constants.TYPE_MOVIE_SERIES, context.getString(R.string.label_series)));
        return list;
    }

}
