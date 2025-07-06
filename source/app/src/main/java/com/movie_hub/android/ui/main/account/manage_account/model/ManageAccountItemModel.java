package com.movie_hub.android.ui.main.account.manage_account.model;

import lombok.Data;

@Data
public class ManageAccountItemModel {
    public int idIcon;
    public int idTitle;

    public ManageAccountItemModel(int idIcon, int idTitle) {
        this.idIcon = idIcon;
        this.idTitle = idTitle;
    }
}

