package com.example.akibapay.ui.carte;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CartesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CartesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notificccccccccccccccations fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}