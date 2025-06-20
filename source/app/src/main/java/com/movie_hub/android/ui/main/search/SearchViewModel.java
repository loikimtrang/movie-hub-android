package com.movie_hub.android.ui.main.search;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class SearchViewModel extends BaseFragmentViewModel {
    public SearchViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
