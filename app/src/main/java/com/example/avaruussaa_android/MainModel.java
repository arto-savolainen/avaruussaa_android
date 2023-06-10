package com.example.avaruussaa_android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.preference.PreferenceManager;

// ViewModel for handling data and user actions for MainActivity
public class MainModel extends AndroidViewModel {
    private static final String TAG = "mainmodeltag";
    private final SavedStateHandle savedStateHandle;
    private final MutableLiveData<String> nameLiveData;
    private final MutableLiveData<String> activityLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Integer> brightnessLiveData;
    private final String NAME_KEY = "NAME";
    private final String ACTIVITY_KEY = "ACTIVITY";
    private final String ERROR_KEY = "ERROR";
    private final String BRIGHTNESS_KEY = "BRIGHTNESS";
    private final int DEFAULT_BRIGHTNESS = 90;
    SharedPreferences.OnSharedPreferenceChangeListener stationListener = null; // Must store a strong reference to listener to prevent GC.
    SharedPreferences.OnSharedPreferenceChangeListener brightnessListener = null;

    public MainModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;

        Context context = getApplication().getApplicationContext();
        nameLiveData = savedStateHandle.getLiveData(NAME_KEY, "");
        activityLiveData = savedStateHandle.getLiveData(ACTIVITY_KEY, "");
        errorLiveData = savedStateHandle.getLiveData(ERROR_KEY, "");
        brightnessLiveData = savedStateHandle.getLiveData(BRIGHTNESS_KEY, DEFAULT_BRIGHTNESS);

        if (activityLiveData.getValue() != null && activityLiveData.getValue().length() == 0) {
            activityLiveData.setValue(context.getString(R.string.main_loading_text));
        }
        if (nameLiveData.getValue() != null && nameLiveData.getValue().length() == 0) {
            nameLiveData.setValue(StationsData.getCurrentStationName(context));
        }

        registerStationChangeListener();
        registerBrightnessChangeListener();
    }

    // Creates and registers a listener for changes to StationStore SharedPreferences.
    // Whenever the currently selected station changes, or a data update is indicated via changing the value of the "refresh" entry,
    // this method fetches data for that station and sets MutableLiveData and SavedState accordingly.
    private void registerStationChangeListener() {
        Context context = getApplication().getApplicationContext();

        SharedPreferences stationStore = context.getSharedPreferences("StationStore", Context.MODE_PRIVATE);
        stationListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d(TAG, "onSharedPreferenceChanged: key: " + key);

                // If the currently selected station has changed, get data for that station and update LiveData.
                if (key != null && key.equals("current_station_name")) {
                    Station currentStation = StationsData.getCurrentStation(context);
                    Log.d(TAG, "currentStation data fetched, here it is: " + currentStation);
                    updateLiveDataAndSavedState(currentStation);
                }

                // After UpdateWorker has finished updating data it changes the value of "refresh".
                if (key != null && key.equals("refresh")) {
                    Log.d(TAG, "Data refresh triggered, updating MutableLiveData and SavedState");
                    Station currentStation = StationsData.getCurrentStation(context);
                    updateLiveDataAndSavedState(currentStation);
                }
            }
        };

        stationStore.registerOnSharedPreferenceChangeListener(stationListener);
    }

    // Creates and registers a listener for the background brightness preference.
    // MainActivity listens for changes in brightnessLiveData and sets background alpha accordingly.
    private void registerBrightnessChangeListener() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
            // Initialize brightness from SharedPreferences on app start
            brightnessLiveData.setValue(prefs.getInt("brightness", DEFAULT_BRIGHTNESS));

            brightnessListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences p, String key) {
                    Log.d(TAG, "onSharedPreferenceChanged: key: " + key);
                    if (key != null && key.equals("brightness")) { // key not to be confused with BRIGHTNESS_KEY
                        int newValue = p.getInt("brightness", DEFAULT_BRIGHTNESS);
                        brightnessLiveData.setValue(newValue);
                        savedStateHandle.set(BRIGHTNESS_KEY, newValue);
                    }
                }
            };

            prefs.registerOnSharedPreferenceChangeListener(brightnessListener);
        }
        catch (Exception e) {
            Log.e(TAG, "registerBrightnessChangeListener: EXCEPTION: " + e);
        }
    }

    private void updateLiveDataAndSavedState(Station currentStation) {
        try {
            Log.d(TAG, "updateLiveDataAndSavedStateHandle: updating LiveData and savedStateHandle. currentStation:" + currentStation);
            nameLiveData.setValue(currentStation.name());
            activityLiveData.setValue(currentStation.activity());
            errorLiveData.setValue(currentStation.error());
            savedStateHandle.set(NAME_KEY, currentStation.name());
            savedStateHandle.set(ACTIVITY_KEY, currentStation.activity());
            savedStateHandle.set(ERROR_KEY, currentStation.error());
        }
        catch (Exception e) {
            Log.e(TAG, "updateLiveDataAndSavedState: EXCEPTION: " + e);
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

    LiveData<Integer> getBrightness() {
        return brightnessLiveData;
    }
}
