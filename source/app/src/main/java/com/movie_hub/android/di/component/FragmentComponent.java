package com.movie_hub.android.di.component;


import com.movie_hub.android.di.module.FragmentModule;
import com.movie_hub.android.di.scope.FragmentScope;
import com.movie_hub.android.ui.main.account.AccountFragment;
import com.movie_hub.android.ui.main.account.UnLoginAccountFragment;
import com.movie_hub.android.ui.main.home.HomeFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.CastFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.EpisodesFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.RecommendationFragment;
import com.movie_hub.android.ui.main.schedule.ScheduleFragment;
import com.movie_hub.android.ui.main.search.SearchFragment;
import com.movie_hub.android.ui.main.search.result.SearchResultFragment;
import com.movie_hub.android.ui.main.search.suggestion.SearchSuggestionFragment;
import com.movie_hub.android.ui.main.search.suggestion.SearchSuggestionViewModel;
import com.movie_hub.android.ui.main.search.topTrending.SearchTopTrendingFragment;
import com.movie_hub.android.ui.main.search.topTrending.SearchTopTrendingViewModel;

import dagger.Component;

@FragmentScope
@Component(modules = {FragmentModule.class},dependencies = AppComponent.class)
public interface FragmentComponent {
    void inject(HomeFragment fragment);
    void inject(SearchFragment fragment);
    void inject(ScheduleFragment fragment);
    void inject(AccountFragment fragment);
    void inject(UnLoginAccountFragment fragment);
    void inject(SearchTopTrendingFragment fragment);
    void inject(SearchSuggestionFragment fragment);
    void inject(SearchResultFragment fragment);
    void inject(CastFragment fragment);
    void inject(RecommendationFragment fragment);
    void inject(EpisodesFragment fragment);
}
