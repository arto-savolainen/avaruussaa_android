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

// This is our data store Singleton which holds information about the magnetic activity and error state of each station
// The data it holds is updated by DataFetchWorker
public class StationsData {
    // It's initialized with the names and codes of each weather station listed at https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa
    // The codes can be used to parse station data from the javascript found on that page
    // Data may be read through stations() and items modified with Station#setActivity() and Station#setError()


    // This is a static class, constructor is private
    private StationsData() {
        // Set Nurmijärvi as default station
//        currentStation = stationsList.get(10).name();
    }

//    public static List<Station> stations() {
//        return stationsList;
//    }

    // This is used to write the name of the currently selected station to SharedPreferences
    // and update data held by subscribers, i.e. the MainModel instance which updates the main view
    public static void setCurrentStation(@NonNull Context context, String stationName) {
//        Log.d("mytag", "IN SETCURRENTSTATION, currenStation: " + currentStation + " stationName: " + stationName);
//        currentStation = stationName;

        // Get latest data from stationsList and pass it to the subscriber (MainModel) in DataMediator
//        for (Station station : stationsList) {
//            if (station.name().equals(stationName)) {
//                DataMediator.notifySubscribers(station);
//            }
//        }

        StationsData.writeStringToPreferences(context, "current_station_name", stationName);
    }

//    public static Station getCurrentStation() {
//        Station curStation = new Station("Debug");
//
//        for (Station station : stationsList) {
//            if (station.name().equals(currentStation)) {
//                curStation = station;
//            }
//        }
//
//        return curStation;
//    }

    // TODO: MAYBE STATIONSDATA EXTENDS APPLICATION SO WE GET CONTEXT
    public static void setStationData(@NonNull Context context, Station station) {
        List<Pair<String, String>> stationPrefList = List.of(
            new Pair<>(station.name() + "_code", station.code()),
            new Pair<>(station.name() + "_activity", station.activity()),
            new Pair<>(station.name() + "_error", station.error())
        );

        StationsData.writeListToPreferences(context, stationPrefList);
    }

    // Reads config data from arrays.xml and returns a list of stations with names and codes
    public static List<Station> getDefaultStationList(@NonNull Context context) {
        Log.d("mytag", "in getDefaultStationListFromResources: ");
        List<Station> stationsList = new ArrayList<>();
        Resources resources = context.getResources();
        String[] names = resources.getStringArray(R.array.station_names);
        String[] codes = resources.getStringArray(R.array.station_codes);

        if (names.length != codes.length) {
            return stationsList;
        }

        for (int i = 0; i < names.length; i++) {
            stationsList.add(new Station(names[i], codes[i]));
        }

        Log.d("mytag", "STATIONSLIST: " + stationsList);

        return stationsList;
    }

    @SuppressLint("ApplySharedPref")
    public static void writeStringToPreferences(@NonNull Context context, @NonNull String key, @NonNull String value) {
        Log.d("stationsdatatag", "Writing String to preferences, key: " + key + ", value: " + value);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stationStore.edit();
        editor.putString(key, value);
        editor.commit();
//        editor.apply();
    }

    // Takes a list of key-value Pairs and writes them to "StationStore" SharedPreferences
    @SuppressLint("ApplySharedPref")
    public static void writeListToPreferences(@NonNull Context context, @NonNull List<Pair<String, String>> prefs) {
        Log.d("utilstag", "writeListToPreferences prefs: " + prefs);
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = stationStore.edit();

        for (Pair<String, String> p : prefs) {
            editor.putString(p.first, p.second);
        }

        editor.commit();
//        editor.apply(); // Will have to test if apply / commit makes any difference in app responsiveness
    }

    //
    public static String getCurrentStationNameFromPreferences(@NonNull Context context) {
        Log.d("utilstag", "Getting current station name from preferences");

        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        return stationStore.getString("current_station_name", context.getResources().getString(R.string.default_station_name));
    }

    public static void refreshSharedPreferences(@NonNull Context context) {
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        Boolean refresh = stationStore.getBoolean("refresh", true);
        refresh = !refresh;

        SharedPreferences.Editor editor = stationStore.edit();
        editor.putBoolean("refresh", refresh);
        editor.commit();
//        editor.apply();
    }

    public static Boolean getRefreshFromPreferences(@NonNull Context context) {
        Log.d("utilstag", "Getting current station name from preferences");

        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        return stationStore.getBoolean("refresh", true);
    }

    public static Station getStationFromPreferences(@NonNull Context context, String stationName) {
        SharedPreferences stationStore = context.getSharedPreferences("StationStore",  Context.MODE_PRIVATE);
        String code = stationStore.getString(stationName + "_code", "ERROR_CODE_NOT_FOUND");
        String activity = stationStore.getString(stationName + "_activity", "");
        String error = stationStore.getString(stationName + "_error", "");

        return new Station(stationName, code, activity, error);
    }
}