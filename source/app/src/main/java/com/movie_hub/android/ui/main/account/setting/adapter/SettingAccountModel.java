package com.movie_hub.android.ui.main.account.setting.adapter;

import lombok.Data;

@Data
public class SettingAccountModel {
    private String name;
    private Integer resolution;
    public boolean isCheck;

    public SettingAccountModel(String string, int i, boolean b) {
        this.name = string;
        this.resolution = i;
        this.isCheck = b;
    }
}
