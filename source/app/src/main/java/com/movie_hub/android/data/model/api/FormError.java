package com.movie_hub.android.data.model.api;

import lombok.Data;

@Data
public class FormError {
    private String field;
    private String message;
}
