package com.movie_hub.android.ui.main.account.language.model;

public class LanguageItemModel {
    public int stringResId;
    public String code;
    public boolean isCheck;

    public LanguageItemModel(int stringResId, String code, Boolean isCheck) {
        this.stringResId = stringResId;
        this.isCheck = isCheck;
        this.code = code;
    }
}

