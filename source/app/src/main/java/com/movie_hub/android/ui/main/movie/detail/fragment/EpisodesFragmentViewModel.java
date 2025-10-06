package com.movie_hub.android.ui.main.movie.detail.fragment;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class EpisodesFragmentViewModel extends BaseFragmentViewModel {
    public EpisodesFragmentViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
