package com.movie_hub.android.ui.base.fragment;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.movie_hub.android.MVVMApplication;
import com.movie_hub.android.data.Repository;
import com.movie_hub.android.data.model.other.ToastMessage;

import java.util.Objects;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import lombok.Setter;

public class BaseFragmentViewModel extends ViewModel {

    protected final Repository repository;
    protected final MVVMApplication application;
    protected final MutableLiveData<ToastMessage> mErrorMessage = new MutableLiveData<>();
    protected final ObservableBoolean mIsLoading = new ObservableBoolean();
    protected CompositeDisposable compositeDisposable;


    @Setter
    protected String token;

    public BaseFragmentViewModel(Repository repository, MVVMApplication application) {
        this.repository = repository;
        this.application = application;
        this.compositeDisposable = new CompositeDisposable();

    }

    public void showSuccessMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_SUCCESS,message));
    }

    public void showNormalMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_NORMAL,message));
    }

    public void showWarningMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_WARNING,message));
    }

    public void showErrorMessage(String message){
        mErrorMessage.setValue(new ToastMessage(ToastMessage.TYPE_ERROR,message));
    }
    public void showLoading(){
        mIsLoading.set(true);
    }

    public void hideLoading(){
        mIsLoading.set(false);
    }

    public boolean isLogin() {
        return repository.getToken() != null && !Objects.equals(repository.getToken(), "") && !Objects.equals(repository.getToken(), "NULL");
    }
}
