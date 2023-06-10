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

        // TODO NOTE: "what you dont hear, is that the application can be killed by the SO without user interaction (to release memory for foreground apps)
        //  and in this scenario when the user tries to come back to your app it will restart from the last used activity and not from the launcher activity"
        // from https://stackoverflow.com/questions/52288361/does-android-kill-singleton-in-order-to-free-memory this requires testing.

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
