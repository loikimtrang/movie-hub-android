package com.movie_hub.android.data;

import com.movie_hub.android.data.local.prefs.PreferencesService;
import com.movie_hub.android.data.local.room.RoomService;
import com.movie_hub.android.data.remote.ApiService;

import javax.inject.Inject;

public class AppRepository implements Repository {

    private final ApiService mApiService;
    private final PreferencesService mPreferencesHelper;
    private final RoomService roomService;

    @Inject
    public AppRepository(PreferencesService preferencesHelper, ApiService apiService, RoomService roomService) {
        this.mPreferencesHelper = preferencesHelper;
        this.mApiService = apiService;
        this.roomService = roomService;
    }

    /**
     * ################################## Preference section ##################################
     */
    @Override
    public String getToken() {
        return mPreferencesHelper.getToken();
    }

    @Override
    public void setToken(String token) {
        mPreferencesHelper.setToken(token);
    }

    @Override
    public PreferencesService getSharedPreferences(){
        return mPreferencesHelper;
    }



    /**
    *  ################################## Remote api ##################################
    */
    @Override
    public ApiService getApiService(){
        return mApiService;
    }


    @Override
    public RoomService getRoomService() {
        return roomService;
    }
}
