package com.movie_hub.android.ui.main.home.filter.model;

import lombok.Data;

@Data
public class FilterTypeModel {
    public FilterTypeModel(String name, int type, boolean isSelect) {
        this.name = name;
        this.type = type;
        this.isSelect = isSelect;
    }

    public FilterTypeModel() {
    }

    private String name;
    private int type;
    private boolean isSelect;
}
