package com.movie_hub.android.ui.main.account;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.api.response.user.UserResponse;
import com.movie_hub.android.data.model.mapper.UserMapper;
import com.movie_hub.android.ui.base.fragment.BaseFragmentViewModel;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class AccountViewModel extends BaseFragmentViewModel {

    private final MutableLiveData<UserResponse> currentUser = new MutableLiveData<>();

    public AccountViewModel(Repository repository, MVVMApplication application) {
        super(repository, application);
    }

    public LiveData<UserResponse> getCurrentUserLiveData() {
        return currentUser;
    }

    @SuppressLint("CheckResult")
    public void getUser() {
        compositeDisposable.add(
                repository.getRoomService().userDao().getCurrentUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(user -> {
                            UserResponse response = UserMapper.toResponse(user);
                            currentUser.setValue(response);
                        }, throwable -> {
                        })
        );

    }
}
