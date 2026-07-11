package com.movie_hub.android.data.model.api.request.forgot;

import lombok.Data;

@Data
public class ForgotChangePasswordRequest {
    private String confirmPassword;
    private String email;
    private String otp;
    private String password;
}
