package com.movie_hub.android.ui.main.live;


import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class LiveViewModel extends BaseFragmentViewModel {

    public LiveViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
