package com.example.avaruussaa_android;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

// This class offers helper functions for retrieving data from PreferenceManager.getDefaultSharedPreferences().
public class AppSettings {
    private final SharedPreferences prefs;
    private static final String TAG = "appsettingstag";

    AppSettings() {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(InitApp.getInstance());
    }

    public double getNotificationThreshold() {
        return getDoubleFromStringPref("threshold", "0.4");
    }

    public double getNotificationInterval() {
        return getDoubleFromStringPref( "interval", "1");
    }

    private double getDoubleFromStringPref(@NonNull String key, @NonNull String defaultValue) {
        String stringValue = prefs.getString(key, defaultValue).replace(',', '.');
        double doubleValue = -1;

        try {
            doubleValue = Double.parseDouble(stringValue);
        }
        catch (NumberFormatException e) {
            Log.e(TAG, "getDoubleFromStringPref: EXCEPTION: " + e);
            e.printStackTrace();
        }

        return doubleValue;
    }

    public Boolean getNotificationsEnabled() {
        return prefs.getBoolean("notifications", InitApp.getInstance().getResources().getBoolean(R.bool.default_notifications_enabled));
    }
}