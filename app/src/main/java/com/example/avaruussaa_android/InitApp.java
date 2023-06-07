package com.example.avaruussaa_android;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.appcompat.app.AppCompatDelegate;


// Application.onCreate() is the first thing that runs when the app is started, initialize processes here
public class InitApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Always use night theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Start periodic work i.e. data fetching at intervals
        WorkController.initWork(getApplicationContext());

        //TODO: REMOVE THIS IS TEMPORARY
        Utils.writeStringToStationStore(getApplicationContext(), "current_station_name", "Tartto");
    }
}
