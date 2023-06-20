package com.example.avaruussaa_android;

import android.app.Application;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

// This class begins the data fetching process by calling WorkController.startWork().
// Also offers a way for other classes to get application context through getInstance().
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

        // Start periodic work i.e. data fetching at intervals.
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
