package com.movie_hub.android.data;

import com.movie_hub.android.data.local.prefs.PreferencesService;
import com.movie_hub.android.data.local.room.RoomService;
import com.movie_hub.android.data.remote.ApiService;
import com.movie_hub.android.data.remote.MasterApiService;


public interface Repository {

    /**
     * ################################## Preference section ##################################
     */
    String getToken();
    void setToken(String token);

    PreferencesService getSharedPreferences();


    /**
     *  ################################## Remote api ##################################
     */
    ApiService getApiService();

    RoomService getRoomService();
    MasterApiService getMasterApiService();

}
