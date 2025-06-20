package com.movie_hub.android.di.component;


import com.movie_hub.android.di.module.FragmentModule;
import com.movie_hub.android.di.scope.FragmentScope;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.UnLoginAccountFragment;
import com.movie_hub.android.ui.main.home.HomeFragment;
import com.movie_hub.android.ui.main.schedule.ScheduleFragment;
import com.movie_hub.android.ui.main.search.SearchFragment;

import dagger.Component;

@FragmentScope
@Component(modules = {FragmentModule.class},dependencies = AppComponent.class)
public interface FragmentComponent {
    void inject(HomeFragment fragment);
    void inject(SearchFragment fragment);
    void inject(ScheduleFragment fragment);
    void inject(AccountFragment fragment);
    void inject(UnLoginAccountFragment fragment);
}
