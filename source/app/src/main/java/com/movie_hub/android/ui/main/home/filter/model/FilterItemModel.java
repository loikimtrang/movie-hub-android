package com.movie_hub.android.ui.main.home.filter.model;

import lombok.Data;

@Data
public class FilterItemModel {
    private String name;

    public FilterItemModel(String name) {
        this.name = name;
    }
}
