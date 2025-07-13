package com.movie_hub.android.ui.main.search.suggestion;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class SearchSuggestionViewModel extends BaseFragmentViewModel {
    public SearchSuggestionViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
