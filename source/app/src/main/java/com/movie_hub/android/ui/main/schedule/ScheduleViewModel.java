package com.movie_hub.android.ui.main.schedule;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

public class ScheduleViewModel extends BaseFragmentViewModel {
    public ScheduleViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
