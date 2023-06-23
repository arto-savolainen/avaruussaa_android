package com.example.avaruussaa_android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.preference.PreferenceManager;

// ViewModel for MainActivity. Listens for changes in SharedPreferences and updates MutableLiveData member
// variables accordingly. These changes in MutableLiveData are observed by MainActivity to update the view.
public class MainModel extends AndroidViewModel implements TimerSubscriber, DefaultLifecycleObserver {
    private static final String TAG = "mainmodeltag";
    private final SavedStateHandle savedStateHandle;
    private MutableLiveData<String> nameLiveData;
    private MutableLiveData<String> activityLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<Integer> brightnessLiveData;
    private MutableLiveData<String> timerLiveData;
    private final String NAME_KEY = "NAME";
    private final String ACTIVITY_KEY = "ACTIVITY";
    private final String ERROR_KEY = "ERROR";
    private final String BRIGHTNESS_KEY = "BRIGHTNESS";
    private final String TIMER_KEY = "TIMER";
    private final int DEFAULT_BRIGHTNESS;
    SharedPreferences.OnSharedPreferenceChangeListener stationListener; // Must store a strong reference to listener to prevent GC.
    SharedPreferences.OnSharedPreferenceChangeListener brightnessListener;
    CountDownTimer timer; // Counts down seconds to the next data update.

    public MainModel(Application application, SavedStateHandle savedStateHandle) {
        super(application);
        Log.d(TAG, "MainModel: IN MAINMODEL CONSTRUCTOR");
        this.savedStateHandle = savedStateHandle;
        DEFAULT_BRIGHTNESS = getApplication().getApplicationContext().getResources().getInteger(R.integer.default_brightness);

        initializeLiveData();
        registerStationChangeListener();
        registerBrightnessChangeListener();
        synchronizeTimer();
    }

    // Initializes MutableLiveData from SavedState, or if SavedState is empty retrieves state from StationsData.
    private void initializeLiveData() {
        Context context = getApplication().getApplicationContext();

        nameLiveData = savedStateHandle.getLiveData(NAME_KEY, "");
        activityLiveData = savedStateHandle.getLiveData(ACTIVITY_KEY, "");
        errorLiveData = savedStateHandle.getLiveData(ERROR_KEY, "");
        brightnessLiveData = savedStateHandle.getLiveData(BRIGHTNESS_KEY, DEFAULT_BRIGHTNESS);
        timerLiveData = new MutableLiveData<>("00:00");

        Log.d(TAG, "LIVEDATA VALUES FROM SAVEDSTATE: name: " + nameLiveData.getValue() + ", activity: " + activityLiveData.getValue() + ", error: " + errorLiveData.getValue() );

        // Here we initialize MutableLiveData in case the task stack disappears and SavedState is lost. Currently this
        // should not happen, but could occur if for, example, the notification intent which launches MainActivity is changed.
        if (errorLiveData.getValue() != null && errorLiveData.getValue().length() == 0) {
            errorLiveData.setValue((StationsData.getCurrentStation(context).error()));
        }
        if (activityLiveData.getValue() != null && activityLiveData.getValue().length() == 0) {
            if (StationsData.isInitialized()) {
                activityLiveData.setValue(StationsData.getCurrentStation(context).activity());
            }
            else {
                activityLiveData.setValue(context.getString(R.string.main_loading_text));
            }
        }
        if (nameLiveData.getValue() != null && nameLiveData.getValue().length() == 0) {
            nameLiveData.setValue(StationsData.getCurrentStationName(context));
        }
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
            // Initialize brightness from SharedPreferences on app start.
            brightnessLiveData.setValue(prefs.getInt("brightness", DEFAULT_BRIGHTNESS));

            brightnessListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences p, String key) {
                    Log.d(TAG, "onSharedPreferenceChanged: key: " + key);
                    if (key != null && key.equals("brightness")) { // key not to be confused with MutableLiveData key BRIGHTNESS_KEY
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

    private void synchronizeTimer() {
        WorkController.subscribe(this);
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

    // This function is called by WorkController every second as the timer it's running updates.
    // It builds a string from the given milliseconds in the format "mm:ss" and sets the MutableLiveData's value to update UI.
    @Override
    public void onTick(long millisUntilFinished) {
        int secondsUntilFinished = (int) millisUntilFinished / 1000;
        int minutes = Math.floorDiv(secondsUntilFinished, 60);
        int seconds = secondsUntilFinished - minutes * 60;
        String minuteString = minutes < 10 ? "0" + minutes :  "" + minutes;
        String secondsString = seconds < 10 ?  "0" + seconds :  "" + seconds;

        // WorkController exists in the main thread so this should be safe.
        timerLiveData.setValue(minuteString + ":" + secondsString);
    }

    // Battery optimization may block background network requests, resulting in an error message.
    // To attempt to fix this we detect the error and trigger a data update when the user resumes using the app.
    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        if (errorLiveData != null && errorLiveData.getValue() != null && errorLiveData.getValue().contains(getApplication().getString(R.string.error_connectivity))) {
            Log.d(TAG, "onResume: !!!!! Error detected, triggering manual update !!!!!");
            WorkController.triggerUpdate();
        }
    }

    @Override
    public void onCleared() {
        WorkController.unsubscribe();
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

    LiveData<String> getTimerString() {
        return timerLiveData;
    }
}
