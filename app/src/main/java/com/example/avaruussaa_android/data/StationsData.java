package com.example.avaruussaa_android.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import com.example.avaruussaa_android.R;
import com.example.avaruussaa_android.data.models.Station;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// This is our data store which holds information about the magnetic activity and error state of each weather station.
// The data is fetched by UpdateWorker, which sends it here; it is then cached and written to SharedPreferences. This
// also triggers a listener function in the view model of the main activity, thus the user interface is updated with new data.
// If the data is garbage collected or the process is killed the data will be retrieved from SharedPreferences.
// Currently most functions require that Context is passed to them in order to write to SharedPreferences. This is because
// I am unsure if it can be guaranteed that InitApp.getInstance() doesn't return null in some situations; namely if
// PeriodicUpdateWorker uses StationsData after the application has been killed / garbage collected.
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
        writeStringToStationStore(context, "current_station_name", stationName);
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
        return getStationFromStationStore(context, stationName);
    }

    // This function can be used to check if station data has been fetched and cache populated.
    // For example, an activity may load before this happens and call this function to see if it
    // should display a loading indicator or get the data.
    public static Boolean isInitialized() {
        return initialized;
    }

    // This function takes a list containing Stations and writes the data to the cache and SharedPreferences.
    public static void setStationsData(@NonNull Context context, List<Station> stationsData) {
        // Create a deep copy of the list so we don't reference data created by UpdateWorker. Data is now cached.
        stationsList = copyStationsList(stationsData);
        writeStationsToStationStore(context, stationsList);

        // Write the time of the update to SharedPrefs so that cache age can be determined (used by PeriodicUpdateWorker).
        writeLastUpdateTimeToStationStore(context);

        initialized = true;

        // This writes a value to SharedPreferences to trigger the listener function in MainModel, which then updates the main view.
        notifyUpdateComplete(context);
    }

    private static void writeLastUpdateTimeToStationStore(@NonNull Context context) {
        long currentTimeMillis = new Date().getTime();
        writeLongToStationStore(context, "last_update_time", currentTimeMillis);
    }

    private static ArrayList<Station> copyStationsList(List<Station> stationsData) {
        ArrayList<Station> newList = new ArrayList<>();

        for (Station station : stationsData) {
            newList.add(station.clone());
        }

        return newList;
    }

    // Takes a list of stations, converts it to key-value Pairs and writes them to "StationStore" SharedPreferences.
    private static void writeStationsToStationStore(@NonNull Context context, @NonNull List<Station> stations) {
        Log.d(TAG, "writeStationsToPreferences: stations: " + stations);
        List<Pair<String, String>> stationPrefList = new ArrayList<>();

        for (Station station : stations) {
            stationPrefList.add(new Pair<>(station.name() + "_code", station.code()));
            stationPrefList.add(new Pair<>(station.name() + "_activity", station.activity()));
            stationPrefList.add(new Pair<>(station.name() + "_error", station.error()));
        }

        writeListToStationStore(context, stationPrefList);
    }

    // General use function for writing a List of key-value Pairs to StationStore SharedPreferences.
    @SuppressLint("ApplySharedPref")
    public static void writeListToStationStore(@NonNull Context context, @NonNull List<Pair<String, String>> prefs) {
        Log.d(TAG, "writeListToPreferences prefs: " + prefs);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stationStore.edit();

        for (Pair<String, String> p : prefs) {
            editor.putString(p.first, p.second);
        }

        editor.commit();
    }

    // Returns a list of station names and codes. If the data is not already cached then reads it from arrays.xml.
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

    // Writes a given string to StationStore SharedPreferences.
    @SuppressLint("ApplySharedPref")
    public static void writeStringToStationStore(@NonNull Context context, @NonNull String key, @NonNull String value) {
        Log.d(TAG, "Writing String to preferences, key: " + key + ", value: " + value);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stationStore.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // Writes a given long value to StationStore SharedPreferences.
    // This is used by UpdateWorker to save the Unix timestamp of the time of the last notification.
    @SuppressLint("ApplySharedPref")
    public static void writeLongToStationStore(@NonNull Context context, @NonNull String key, long value) {
        Log.d(TAG, "Writing long to preferences, key: " + key + ", value: " + value);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stationStore.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    // Retrieves a long value from StationStore SharedPreferences.
    // This is used by UpdateWorker to get the Unix timestamp of the time of the last notification.
    public static long getLongFromStationStore(@NonNull Context context, @NonNull String key, long defaultValue) {
        Log.d(TAG, "Retrieving Long from preferences, key: " + key);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", Context.MODE_PRIVATE);
        return stationStore.getLong(key, defaultValue);
    }

    // Retrieves the name of the currently selected station. Gets it from SharedPreferences if it's not cached yet.
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
    @SuppressLint("ApplySharedPref")
    public static void notifyUpdateComplete(@NonNull Context context) {
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        boolean refresh = stationStore.getBoolean("refresh", true);
        refresh = !refresh;

        SharedPreferences.Editor editor = stationStore.edit();
        editor.putBoolean("refresh", refresh);
        editor.commit();
    }

    public static Station getStation(@NonNull Context context, @NonNull String stationName) {
        if (stationsList != null) {
            return findStationData(context, stationName);
        }

        return getStationFromStationStore(context, stationName);
    }

    // Retrieves the data of the currently selected station.
    public static Station getCurrentStation(@NonNull Context context) {
        String currentStationName = getCurrentStationName(context);

        if (stationsList != null) {
            return findStationData(context, currentStationName);
        }

        // This shouldn't be reached given how the program is currently structured.
        // In practice getCurrentStation() is never called before the cache is initialized.
        return getStationFromStationStore(context, currentStationName);
    }

    private static Station getStationFromStationStore(@NonNull Context context, @NonNull String stationName) {
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