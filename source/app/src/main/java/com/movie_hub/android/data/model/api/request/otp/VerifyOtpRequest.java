package com.movie_hub.android.data.model.api.request.otp;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otp;
}
