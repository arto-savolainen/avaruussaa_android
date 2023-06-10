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

// This is our data store Singleton which holds information about the magnetic activity and error state of each station.
// The data it holds is updated by DataFetchWorker.
// TODO: MAYBE STATIONSDATA EXTENDS APPLICATION? Is that a good idea? I don't know
// TODO: Write the list of stations at once to avoid SharedPreferences overhead on every write
public class StationsData {
    // TODO: Clean up this file
    // stationsList caches data that is written to SharedPreferences. Cache is always written first so should be up-to-date.
    private static List<Station> stationsList = null;
    private static final String TAG = "stationsdatatag";


    // This is a static class, constructor is private.
    private StationsData() {
    }

    // This is used to write the name of the currently selected station to SharedPreferences.
    // and update data held by subscribers, i.e. the MainModel instance which updates the main view.
    public static void setCurrentStation(@NonNull Context context, String stationName) {
        Log.d(TAG, "IN SETCURRENTSTATION, parameter stationName: " + stationName);
        StationsData.writeStringToPreferences(context, "current_station_name", stationName);
    }

    // Find station data of name from the list, if data is not set yet gets it from SharedPreferences
    private static Station findStationData(@NonNull Context context, @NonNull String stationName) {
        if (stationsList != null) {
            for (Station station : stationsList) {
                if (station.name().equals(stationName) && !station.activity().contains(context.getString(R.string.main_loading_text))) {
                    Log.d(TAG, "findStation: hit cache, returning station: " + station);
                    return station;
                }
            }
        }

        Log.d(TAG, "findStation: NO cache hit, returning sharedprefs");
        return getStationFromSharedPreferences(context, stationName);
    }

    // maybe used later?
//    public static void setStationData(@NonNull Context context, Station station) {
//        // Write data to cache first.
//        if (stationsList != null) {
//            Station stationToEdit = findStationToEdit(context, station.name());
//            Log.d(TAG, "setStationData: station: " + station + " writing to cache of station: " + stationToEdit);
//            stationToEdit.setActivity(station.activity());
//            stationToEdit.setError(station.error());
//
//            Log.d(TAG, "setStationData: END OF CACHE WRITE stationToEdit: " + stationToEdit + " station: " + station);
//        }
//
//        List<Pair<String, String>> stationPrefList = List.of(
//            new Pair<>(station.name() + "_code", station.code()),
//            new Pair<>(station.name() + "_activity", station.activity()),
//            new Pair<>(station.name() + "_error", station.error())
//        );
//
//        StationsData.writeListToPreferences(context, stationPrefList);
//    }

    public static void setStationActivity(@NonNull Context context, String stationName, String activity) {
        Station stationToEdit = findStationToEdit(context, stationName);

        // Write data to cache first.
        if (stationsList != null) {
            Log.d(TAG, "setStationACTIVITY: writing to station: " + stationToEdit);
            stationToEdit.setActivity(activity);

            Log.d(TAG, "setStationACTIVITY: END OF CACHE WRITE stationToEdit: " + stationToEdit);
        }

        List<Pair<String, String>> stationPrefList = List.of(
            new Pair<>(stationName + "_code", stationToEdit.code()),
            new Pair<>(stationName + "_activity", stationToEdit.activity()),
            new Pair<>(stationName + "_error", stationToEdit.error())
        );

        StationsData.writeListToPreferences(context, stationPrefList);
    }

    public static void setStationError(@NonNull Context context, String stationName, String error) {
        Station stationToEdit = findStationToEdit(context, stationName);

        // Write data to cache first.
        if (stationsList != null) {
            Log.d(TAG, "setStationERROR: writing to station: " + stationToEdit);
            stationToEdit.setError(error);

            Log.d(TAG, "setStationERROR: END OF CACHE WRITE stationToEdit: " + stationToEdit);
        }

        List<Pair<String, String>> stationPrefList = List.of(
            new Pair<>(stationName + "_code", stationToEdit.code()),
            new Pair<>(stationName + "_activity", stationToEdit.activity()),
            new Pair<>(stationName + "_error", stationToEdit.error())
        );

        StationsData.writeListToPreferences(context, stationPrefList);
    }

    private static Station findStationToEdit(@NonNull Context context, @NonNull String stationName) {
        if (stationsList != null) {
            for (Station station : stationsList) {
                if (station.name().equals(stationName)) {
                    Log.d(TAG, "findStationToEdit: returning station: " + station);
                    return station;
                }
            }
        }

        return new Station("error", "error"); // This should never happen.
    }

    // Returns a list of station names and codes. Activity and error fields are empty.
    // If the data is not already cached then reads it from arrays.xml.
    public static List<Station> getDefaultStationsList(@NonNull Context context) {
        if (stationsList != null) {
            Log.d(TAG, "in getDefaultStationListFromResources: returning cached list");
            return stationsList;
        }

        Log.d(TAG, "in getDefaultStationListFromResources: ");
        List<Station> stations = new ArrayList<>();
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

    // Takes a list of key-value Pairs and writes them to "StationStore" SharedPreferences.
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

    public static String getCurrentStationName(@NonNull Context context) {
        Log.d(TAG, "Getting current station name from preferences");

        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        return stationStore.getString("current_station_name", context.getResources().getString(R.string.default_station_name));
    }

    // This function changes the value of the "refresh" entry in SharedPreferences, thus triggering
    // onSharedPreferenceChanged() in MainModel to let the main view know data has been updated.
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

    private static Station getStationFromSharedPreferences(@NonNull Context context, @NonNull String stationName) {
        String stationCode = findStationCode(stationName);

        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        String code = stationStore.getString(stationName + "_code", stationCode);
        String activity = stationStore.getString(stationName + "_activity", "");
        String error = stationStore.getString(stationName + "_error", "");

        return new Station(stationName, code, activity, error);
    }

    private static String findStationCode(String stationName) {
        if (stationsList != null) {
            for (Station station : stationsList) {
                if (station.name().equals(stationName)) {
                    return station.code();
                }
            }
        }

        return "ERROR_CODE_NOT_FOUND"; // This should never happen.
    }
}