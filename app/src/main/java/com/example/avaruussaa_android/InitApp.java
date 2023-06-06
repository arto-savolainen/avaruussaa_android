package com.example.avaruussaa_android;

import android.app.Application;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;


// Application.onCreate() is the first thing that runs when the app is started, initialize processes here
public class InitApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

//        for (Station station : StationsData.stations()) {
//            Log.d("mytag", station.toString());
//        }

        WorkController.initWork(getApplicationContext());
    }
}
