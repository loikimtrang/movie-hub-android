package com.movie_hub.android.data.model.api.request.movie.filter;

import lombok.Data;

@Data
public class CountryRequest {
    private String value;
    private String label;
    private boolean isSelect;

    public CountryRequest() {
    }

    public CountryRequest(String value, String label, boolean isSelect) {
        this.value = value;
        this.label = label;
        this.isSelect = isSelect;
    }
}

