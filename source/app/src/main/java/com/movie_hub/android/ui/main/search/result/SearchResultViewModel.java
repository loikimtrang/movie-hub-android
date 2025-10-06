package com.movie_hub.android.ui.main.search.result;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class SearchResultViewModel extends BaseFragmentViewModel {
    public SearchResultViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
