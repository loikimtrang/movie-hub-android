package com.movie_hub.android.data.model.api.request.movie.filter;

import lombok.Data;

@Data
public class LanguageRequest {
    private String value;
    private String label;
    private boolean isSelect;

    public LanguageRequest() {
    }

    public LanguageRequest(String value, String label, boolean isSelect) {
        this.value = value;
        this.label = label;
        this.isSelect = isSelect;
    }
}

