package com.movie_hub.android.ui.main.person.fragment;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class MoviesInvolvedFragmentViewModel extends BaseFragmentViewModel {
    public MoviesInvolvedFragmentViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
