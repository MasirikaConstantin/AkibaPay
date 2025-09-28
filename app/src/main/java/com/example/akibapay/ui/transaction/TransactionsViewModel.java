package com.example.akibapay.ui.transaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TransactionsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public TransactionsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}