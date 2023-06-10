package com.example.avaruussaa_android;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;


// Application.onCreate() is the first thing that runs when the app is started, initialize processes here.
public class InitApp extends Application {
    private static final String TAG = "inittag";

    public InitApp() {
//        StrictMode.enableDefaults();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Always use night theme.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Start periodic work i.e. data fetching at intervals.
        WorkController.initWork(getApplicationContext());
    }

    // For emulation use only, will never be called on a production android device.
    @Override
    public void onTerminate() {
        super.onTerminate();

        Log.d(TAG, "Application onTerminate");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        Log.d(TAG, "Application onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Log.d(TAG, "Application onTrimMemory: level: " + level);
    }
}
