package com.movie_hub.android.ui.main.person;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.response.person.PersonResponse;
import com.movie_hub.android.ui.base.activity.BaseViewModel;

public class PersonDetailViewModel extends BaseViewModel {
    PersonResponse person = new PersonResponse();
    public PersonDetailViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }
}
