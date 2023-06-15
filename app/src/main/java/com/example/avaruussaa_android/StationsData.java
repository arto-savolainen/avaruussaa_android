package com.example.avaruussaa_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

// This is our data store which holds information about the magnetic activity and error state of each station.
// The data it holds is updated by UpdateWorker. If the data is garbage collected it will be retrieved from SharedPreferences.
// TODO: MAYBE STATIONSDATA EXTENDS APPLICATION? Is that a good idea? I don't know
// TODO: Clean up this file
public class StationsData {
    // stationsList caches data that is written to SharedPreferences. Cache is always written first so should be up-to-date.
    private static ArrayList<Station> stationsList = null;
    private static String currentStationName = null;
    private static Boolean initialized = false;
    private static final String TAG = "stationsdatatag";

    // This is a static class, constructor is private.
    private StationsData() {
    }

    // This is used to write the name of the currently selected station to SharedPreferences.
    // and update data held by subscribers, i.e. the MainModel instance which updates the main view.
    public static void setCurrentStation(@NonNull Context context, String stationName) {
        Log.d(TAG, "IN SETCURRENTSTATION, parameter stationName: " + stationName);
        currentStationName = stationName;
        StationsData.writeStringToPreferences(context, "current_station_name", stationName);
    }

    // Find station data of name from the list, if data is not set yet gets it from SharedPreferences
    private static Station findStationData(@NonNull Context context, @NonNull String stationName) {
        if (stationsList != null) {
            for (Station station : stationsList) {
                if (station.name().equals(stationName) && !station.activity().contains(context.getString(R.string.main_loading_text))) {
                    Log.d(TAG, "findStation: CACHE HIT, returning station: " + station);
                    return station;
                }
            }
        }

        Log.d(TAG, "findStation: CACHE MISS, returning sharedprefs");
        return getStationFromSharedPreferences(context, stationName);
    }

    public static Boolean isInitialized() {
        return initialized;
    }

    // This function takes a list containing Stations and writes the data to the cache and SharedPreferences.
    public static void setStationsData(@NonNull Context context, List<Station> stationsData) {
        if (stationsData.size() != 12) {
            Log.e(TAG, "setStationsData: ERROR invalid list size");
            return;
        }

        // Create a deep copy of the list so we don't reference data created by UpdateWorker. Data is now cached.
        stationsList = copyStationsList(stationsData);
        writeStationsToPreferences(context, stationsList);

        initialized = true;

        // This writes to SharedPreferences and fires the listener function in MainModel which updates the main view.
        StationsData.notifyUpdateComplete(context);
    }

    private static ArrayList<Station> copyStationsList(List<Station> stationsData) {
        ArrayList<Station> newList = new ArrayList<>();
        for (Station station : stationsData) {
            newList.add(station.clone());
        }

        return newList;
    }

    // Takes a list of stations, converts it to key-value Pairs and writes them to "StationStore" SharedPreferences.
    private static void writeStationsToPreferences(@NonNull Context context, @NonNull List<Station> stations) {
        Log.d(TAG, "writeStationsToPreferences: stations: " + stations);
        List<Pair<String, String>> stationPrefList = new ArrayList<>();

        for (Station station : stations) {
            stationPrefList.add(new Pair<>(station.name() + "_code", station.code()));
            stationPrefList.add(new Pair<>(station.name() + "_activity", station.activity()));
            stationPrefList.add(new Pair<>(station.name() + "_error", station.error()));
        }

        writeListToPreferences(context, stationPrefList);
    }

    // General use function for writing a List of key-value Pairs to StationStore SharedPreferences
    @SuppressLint("ApplySharedPref")
    public static void writeListToPreferences(@NonNull Context context, @NonNull List<Pair<String, String>> prefs) {
        Log.d(TAG, "writeListToPreferences prefs: " + prefs);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stationStore.edit();

        for (Pair<String, String> p : prefs) {
            editor.putString(p.first, p.second);
        }

        editor.commit();
//        editor.apply(); // Could test if apply / commit makes any difference in app responsiveness.
    }

    // Returns a list of station names and codes. Activity and error fields are empty.
    // If the data is not already cached then reads it from arrays.xml.
    public static List<Station> getDefaultStationsList(@NonNull Context context) {
        if (stationsList != null) {
            Log.d(TAG, "in getDefaultStationListFromResources: returning cached list");
            return stationsList;
        }

        Log.d(TAG, "in getDefaultStationListFromResources: ");
        ArrayList<Station> stations = new ArrayList<>();
        Resources resources = context.getResources();
        String[] names = resources.getStringArray(R.array.station_names);
        String[] codes = resources.getStringArray(R.array.station_codes);

        if (names.length != codes.length) {
            return stations;
        }

        for (int i = 0; i < names.length; i++) {
            stations.add(new Station(names[i], codes[i]));
        }

        Log.d(TAG, "STATIONSLIST: " + stations);

        stationsList = stations;
        return stations;
    }

    @SuppressLint("ApplySharedPref")
    public static void writeStringToPreferences(@NonNull Context context, @NonNull String key, @NonNull String value) {
        Log.d(TAG, "Writing String to preferences, key: " + key + ", value: " + value);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stationStore.edit();
        editor.putString(key, value);
        editor.commit();
//        editor.apply();
    }

    public static String getCurrentStationName(@NonNull Context context) {
        if (currentStationName != null) {
            Log.d(TAG, "CACHE HIT, returning current station name from CACHE");
            return currentStationName;
        }

        Log.d(TAG, "CACHE MISS, getting current station name from PREFERENCES");
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        currentStationName = stationStore.getString("current_station_name", context.getResources().getString(R.string.default_station_name));
        return currentStationName;
    }

    // This function changes the value of the "refresh" entry in SharedPreferences, thus triggering
    // onSharedPreferenceChanged() in MainModel and letting the main view know that data has been updated.
    public static void notifyUpdateComplete(@NonNull Context context) {
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        Boolean refresh = stationStore.getBoolean("refresh", true);
        refresh = !refresh;

        SharedPreferences.Editor editor = stationStore.edit();
        editor.putBoolean("refresh", refresh);
        editor.commit();
//        editor.apply();
    }

    public static Station getStation(@NonNull Context context, @NonNull String stationName) {
        if (stationsList != null) {
            return findStationData(context, stationName);
        }

        return getStationFromSharedPreferences(context, stationName);
    }

    public static Station getCurrentStation(@NonNull Context context) {
        String currentStationName = getCurrentStationName(context);

        if (stationsList != null) {
            return findStationData(context, currentStationName);
        }

        // This shouldn't be reached given how the program is currently structured.
        // In practice getCurrentStation() is never called before the cache is initialized.
        return getStationFromSharedPreferences(context, currentStationName);
    }

    private static Station getStationFromSharedPreferences(@NonNull Context context, @NonNull String stationName) {
        // By finding the station's code we can form a valid Station even if it's not found in StationStore.
        // This shouldn't be required I think, but it's here for safety.
        String stationCode = findStationCode(context, stationName);

        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        String code = stationStore.getString(stationName + "_code", stationCode);
        String activity = stationStore.getString(stationName + "_activity", "");
        String error = stationStore.getString(stationName + "_error", "");

        return new Station(stationName, code, activity, error);
    }

    // Finds a station's code from the cache, or if that does not exist, arrays.xml
    private static String findStationCode(@NonNull Context context, @NonNull String stationName) {
        if (stationsList != null) {
            for (Station station : stationsList) {
                if (station.name().equals(stationName)) {
                    return station.code();
                }
            }
        }

        for (Station station : getDefaultStationsList(context)) {
            if (station.name().equals(stationName)) {
                return station.code();
            }
        }

        return "ERROR_CODE_NOT_FOUND"; // This should never be reached.
    }
}