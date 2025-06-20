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
import com.movie_hub.android.ui.main.home.HomeViewModel;
import com.movie_hub.android.ui.main.schedule.ScheduleViewModel;
import com.movie_hub.android.ui.main.search.SearchViewModel;

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
}
