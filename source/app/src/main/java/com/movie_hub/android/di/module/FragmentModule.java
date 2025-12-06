package com.movie_hub.android.di.module;

import android.content.Context;

import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.ViewModelProviderFactory;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.di.scope.FragmentScope;
import com.movie_hub.android.ui.base.fragment.BaseFragment;
import com.movie_hub.android.ui.main.account.AccountViewModel;
import com.movie_hub.android.ui.main.account.UnLoginAccountFragment;
import com.movie_hub.android.ui.main.account.UnLoginAccountViewModel;
import com.movie_hub.android.ui.main.account.favourite.fragment.MovieFavoriteViewModel;
import com.movie_hub.android.ui.main.home.HomeViewModel;
import com.movie_hub.android.ui.main.movie.detail.fragment.CastFragmentViewModel;
import com.movie_hub.android.ui.main.movie.detail.fragment.EpisodesFragmentViewModel;
import com.movie_hub.android.ui.main.movie.detail.fragment.RecommendationFragment;
import com.movie_hub.android.ui.main.movie.detail.fragment.RecommendationFragmentViewModel;
import com.movie_hub.android.ui.main.movie.detail.review.ReviewViewModel;
import com.movie_hub.android.ui.main.person.fragment.InformationFragmentViewModel;
import com.movie_hub.android.ui.main.person.fragment.MoviesInvolvedFragmentViewModel;
import com.movie_hub.android.ui.main.schedule.ScheduleViewModel;
import com.movie_hub.android.ui.main.search.SearchViewModel;
import com.movie_hub.android.ui.main.search.result.SearchResultFragment;
import com.movie_hub.android.ui.main.search.result.SearchResultViewModel;
import com.movie_hub.android.ui.main.search.suggestion.SearchSuggestionViewModel;
import com.movie_hub.android.ui.main.search.topTrending.SearchTopTrendingViewModel;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {

    private BaseFragment<?, ?> fragment;

    public FragmentModule(BaseFragment<?, ?> fragment) {
        this.fragment = fragment;
    }

    @Named("access_token")
    @Provides
    @FragmentScope
    String provideToken(Repository repository) {
        return repository.getToken();
    }
    @Provides
    @FragmentScope
    HomeViewModel provideHomeViewModel(Repository repository, Context application) {
        Supplier<HomeViewModel> supplier = () -> new HomeViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<HomeViewModel> factory = new ViewModelProviderFactory<>(HomeViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(HomeViewModel.class);
    }

    @Provides
    @FragmentScope
    SearchViewModel provideSearchViewModel(Repository repository, Context application) {
        Supplier<SearchViewModel> supplier = () -> new SearchViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<SearchViewModel> factory = new ViewModelProviderFactory<>(SearchViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(SearchViewModel.class);
    }

    @Provides
    @FragmentScope
    ScheduleViewModel provideHomeViewModelScheduleViewModel(Repository repository, Context application) {
        Supplier<ScheduleViewModel> supplier = () -> new ScheduleViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<ScheduleViewModel> factory = new ViewModelProviderFactory<>(ScheduleViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(ScheduleViewModel.class);
    }

    @Provides
    @FragmentScope
    AccountViewModel provideAccountViewModel(Repository repository, Context application) {
        Supplier<AccountViewModel> supplier = () -> new AccountViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<AccountViewModel> factory = new ViewModelProviderFactory<>(AccountViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(AccountViewModel.class);
    }

    @Provides
    @FragmentScope
    UnLoginAccountViewModel provideUnLoginAccountViewModel(Repository repository, Context application) {
        Supplier<UnLoginAccountViewModel> supplier = () -> new UnLoginAccountViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<UnLoginAccountViewModel> factory = new ViewModelProviderFactory<>(UnLoginAccountViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(UnLoginAccountViewModel.class);
    }

    @Provides
    @FragmentScope
    SearchTopTrendingViewModel provideSearchTopTrendingViewModel(Repository repository, Context application) {
        Supplier<SearchTopTrendingViewModel> supplier = () -> new SearchTopTrendingViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<SearchTopTrendingViewModel> factory = new ViewModelProviderFactory<>(SearchTopTrendingViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(SearchTopTrendingViewModel.class);
    }

    @Provides
    @FragmentScope
    SearchSuggestionViewModel provideSearchSuggestionViewModel(Repository repository, Context application) {
        Supplier<SearchSuggestionViewModel> supplier = () -> new SearchSuggestionViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<SearchSuggestionViewModel> factory = new ViewModelProviderFactory<>(SearchSuggestionViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(SearchSuggestionViewModel.class);
    }

    @Provides
    @FragmentScope
    SearchResultViewModel provideSearchResultViewModel(Repository repository, Context application) {
        Supplier<SearchResultViewModel> supplier = () -> new SearchResultViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<SearchResultViewModel> factory = new ViewModelProviderFactory<>(SearchResultViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(SearchResultViewModel.class);
    }

    @Provides
    @FragmentScope
    CastFragmentViewModel provideCastFragmentViewModel(Repository repository, Context application) {
        Supplier<CastFragmentViewModel> supplier = () -> new CastFragmentViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<CastFragmentViewModel> factory = new ViewModelProviderFactory<>(CastFragmentViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(CastFragmentViewModel.class);
    }

    @Provides
    @FragmentScope
    RecommendationFragmentViewModel provideRecommendationFragmentViewModel(Repository repository, Context application) {
        Supplier<RecommendationFragmentViewModel> supplier = () -> new RecommendationFragmentViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<RecommendationFragmentViewModel> factory = new ViewModelProviderFactory<>(RecommendationFragmentViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(RecommendationFragmentViewModel.class);
    }

    @Provides
    @FragmentScope
    EpisodesFragmentViewModel provideEpisodesFragmentViewModel(Repository repository, Context application) {
        Supplier<EpisodesFragmentViewModel> supplier = () -> new EpisodesFragmentViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<EpisodesFragmentViewModel> factory = new ViewModelProviderFactory<>(EpisodesFragmentViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(EpisodesFragmentViewModel.class);
    }

    @Provides
    @FragmentScope
    InformationFragmentViewModel provideInformationFragmentViewModel(Repository repository, Context application) {
        Supplier<InformationFragmentViewModel> supplier = () -> new InformationFragmentViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<InformationFragmentViewModel> factory = new ViewModelProviderFactory<>(InformationFragmentViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(InformationFragmentViewModel.class);
    }

    @Provides
    @FragmentScope
    MoviesInvolvedFragmentViewModel provideMoviesInvolvedFragmentViewModel(Repository repository, Context application) {
        Supplier<MoviesInvolvedFragmentViewModel> supplier = () -> new MoviesInvolvedFragmentViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<MoviesInvolvedFragmentViewModel> factory = new ViewModelProviderFactory<>(MoviesInvolvedFragmentViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(MoviesInvolvedFragmentViewModel.class);
    }

    @Provides
    @FragmentScope
    MovieFavoriteViewModel provideMovieFavoriteViewModel(Repository repository, Context application) {
        Supplier<MovieFavoriteViewModel> supplier = () -> new MovieFavoriteViewModel(repository, (MVVMApplication)application);
        ViewModelProviderFactory<MovieFavoriteViewModel> factory = new ViewModelProviderFactory<>(MovieFavoriteViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(MovieFavoriteViewModel.class);
    }
}
