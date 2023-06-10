package com.example.avaruussaa_android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;


// ViewModel for handling data and user actions for MainActivity
public class MainModel extends AndroidViewModel {
    private static final String TAG = "mainmodeltag";
    private final SavedStateHandle savedStateHandle;
    private final MutableLiveData<String> nameLiveData;
    private final MutableLiveData<String> activityLiveData;
    private final MutableLiveData<String> errorLiveData;
    SharedPreferences.OnSharedPreferenceChangeListener listener = null;

    public MainModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;

        Context context = getApplication().getApplicationContext();
        nameLiveData = savedStateHandle.getLiveData("NAME", "");
        activityLiveData = savedStateHandle.getLiveData("ACTIVITY", "");
        errorLiveData = savedStateHandle.getLiveData("ERROR", "");

        if (activityLiveData.getValue() != null && activityLiveData.getValue().length() == 0) {
            activityLiveData.setValue(context.getString(R.string.main_loading_text));
        }
        if (nameLiveData.getValue() != null && nameLiveData.getValue().length() == 0) {
            nameLiveData.setValue(StationsData.getCurrentStationName(context));
        }

        registerStationChangeListener();
    }

    // Creates and registers a listener for changes to StationStore SharedPreferences
    // Whenever the currently selected station changes, or a data update is indicated via changing the value of the "refresh" entry,
    // this method fetches data for that station and sets MutableLiveData and SavedState accordingly
    private void registerStationChangeListener() {
        Context context = getApplication().getApplicationContext();

        SharedPreferences stationStore = context.getSharedPreferences("StationStore", Context.MODE_PRIVATE);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d(TAG, "onSharedPreferenceChanged: key: " + key);

                // If the currently selected station has changed, get data for that station and update LiveData
                if (key != null && key.equals("current_station_name")) {
                    Station currentStation = StationsData.getStation(context, StationsData.getCurrentStationName(context));
                    Log.d(TAG, "currentStation data fetched, here it is: " + currentStation);
                    updateLiveDataAndSavedState(currentStation);
                }

                // After UpdateWorker has finished updating data it changes the value of "refresh"
                if (key != null && key.equals("refresh")) {
                    Log.d(TAG, "Data refresh triggered, updating MutableLiveData and SavedState");
                    Station currentStation = StationsData.getStation(context, StationsData.getCurrentStationName(context));
                    updateLiveDataAndSavedState(currentStation);
                }
            }
        };

        stationStore.registerOnSharedPreferenceChangeListener(listener);
    }

    private void updateLiveDataAndSavedState(Station currentStation) {
        try {
            Log.d(TAG, "updateLiveDataAndSavedStateHandle: updating LiveData and savedStateHandle. currentStation:" + currentStation);
            nameLiveData.setValue(currentStation.name());
            activityLiveData.setValue(currentStation.activity());
            errorLiveData.setValue(currentStation.error());
            savedStateHandle.set("NAME", currentStation.name());
            savedStateHandle.set("ACTIVITY", currentStation.activity());
            savedStateHandle.set("ERROR", currentStation.error());
        }
        catch (Exception e) {
            Log.d(TAG, "updateLiveDataAndSavedState: EXCEPTION: " + e);
        }
    }

    @Override
    public void onCleared() {
    }

    LiveData<String> getName() {
        return nameLiveData;
    }

    LiveData<String> getActivity() {
        return activityLiveData;
    }

    LiveData<String> getError() {
        return errorLiveData;
    }
}
