package com.movie_hub.android.ui.main.account;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class AccountViewModel extends BaseFragmentViewModel {
    public AccountViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
