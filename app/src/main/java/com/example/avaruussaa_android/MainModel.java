package com.example.avaruussaa_android;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// ViewModel for handling data and user actions for MainActivity
public class MainModel extends ViewModel {
    private static final String TAG = "mainmodeltag";
    // Information of the station the user has selected for displaying on MainActivity
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> activity = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public MainModel() {
        Log.d(TAG, "MAINMODEL CONSTRUCTOR");
        Log.d(TAG, "STATION NAME AT FIRST: " + name.getValue());
        Log.d(TAG, "CURRENT ACTIVITY AT FIRST: " + activity.getValue());

        DataMediator.subscribe(this);
    }

    @Override
    public void onCleared() {
        DataMediator.unsubscribe();
    }

    // This is called by DataMediator after station data has been updated
    public void updateModelData(Station station) {
        Log.d("mytag", "IN UPDATEMODELDATA, station: " + station);
        name.postValue(station.name());
        activity.postValue(station.activity());
        error.postValue(station.error());

        Log.d(TAG, "MAINMODEL CONSTRUCTOR");
        Log.d(TAG, "UPDATEMODELDATA CURRENT NAME: " + name.getValue());
        Log.d(TAG, "UPDATEMODELDATA CURRENT ACTIVITY: " + activity.getValue());
        Log.d(TAG, "UPDATEMODELDATA CURRENT ERROR: " + error.getValue());
    }

    LiveData<String> getName() {
        return name;
    }

    LiveData<String> getActivity() {
        return activity;
    }

    LiveData<String> getError() {
        return error;
    }


}
