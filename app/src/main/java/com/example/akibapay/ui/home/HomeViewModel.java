package com.example.akibapay.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.Arrays;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private final MutableLiveData<List<String>> mItems;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");

        mItems = new MutableLiveData<>();
        // Données d'exemple
        mItems.setValue(Arrays.asList(
                "Transaction 1", "Transaction 2", "Transaction 3",
                "Transaction 4", "Transaction 5", "Transaction 6"
        ));
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<List<String>> getItems() {
        return mItems;
    }
}