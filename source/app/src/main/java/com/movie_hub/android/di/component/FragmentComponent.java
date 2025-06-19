package com.movie_hub.android.di.component;


import com.movie_hub.android.di.module.FragmentModule;
import com.movie_hub.android.di.scope.FragmentScope;

import dagger.Component;

@FragmentScope
@Component(modules = {FragmentModule.class},dependencies = AppComponent.class)
public interface FragmentComponent {

}
