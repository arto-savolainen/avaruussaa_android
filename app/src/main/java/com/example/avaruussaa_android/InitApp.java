package com.example.avaruussaa_android;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.avaruussaa_android.data.WorkController;

// This is our custom Application class. It begins the data fetching process to retrieve weather station data
// from the internet by calling WorkController.startWork(). Also offers a static function for other classes
// to get application context through getInstance().
public class InitApp extends Application {
    private static final String TAG = "inittag";
    private static InitApp instance;

    public InitApp() {
//        StrictMode.enableDefaults();
    }

    public static InitApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Always use night theme.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Start the UpdateWorkers, i.e. data fetching at intervals.
        WorkController.startWork();
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
