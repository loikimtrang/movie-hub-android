package com.movie_hub.android.ui.main.account.playlist;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.activity.BaseViewModel;

public class PlayListViewModel extends BaseViewModel {
    public PlayListViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
