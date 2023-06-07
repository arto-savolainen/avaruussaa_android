package com.example.avaruussaa_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.regex.Pattern;

// Static class for small utility functions
public class Utils {
    public static String[] splitString(@NonNull String string, @NonNull String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter)).split(string);
    }

    public static void removeTrailingZeroes(@NonNull String string) {
        // TODO implementation
    }

    // Takes key and value Strings and writes them to "StationStore" SharedPreferences
    @SuppressLint("ApplySharedPref")
    public static void writeStringToStationStore(@NonNull Context context, @NonNull String key, @NonNull String value) {
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", 0);
        Editor editor = stationStore.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // Takes a list of key-value Pairs and writes them to "StationStore" SharedPreferences
    // Note: currently not used, may remove later
    @SuppressLint("ApplySharedPref")
    public static void writeStringsToStationStore(@NonNull Context context, @NonNull List<Pair<String, String>> prefs) {
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", 0);
        Editor editor = stationStore.edit();
        editor.clear(); // Clear values to remove old errors

        for (Pair<String, String> p : prefs) {
            editor.putString(p.first, p.second);
        }

        editor.commit();
    }

    // Gets the value of a String preference of name "key", returns empty string if no preference is found
    public static String getStringFromSharedPreferences(@NonNull Context context, String key) {
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", 0);
        return stationStore.getString(key, "");
    }
}