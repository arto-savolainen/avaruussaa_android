package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

// This is our data store Singleton which holds information about the magnetic activity and error state of each station
// The data it holds is updated by DataFetchWorker
public class StationsData {
    // stationsList is an unmodifiableList, encapsulating our station data and preventing direct modification of the list
    // It's initialized with the names and codes of each weather station listed at https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa
    // The codes can be used to parse station data from the javascript found on that page
    // Data may be read through stations() and items modified with Station#setActivity() and Station#setError()
    private static final List<Station> stationsList = List.of(
        new Station("Kevo", "KEV"),
        new Station("Kilpisjärvi", "KIL"),
        new Station("Ivalo", "IVA"),
        new Station("Muonio", "MUO"),
        new Station("Sodankylä", "SOD"),
        new Station("Pello", "PEL"),
        new Station("Ranua", "RAN"),
        new Station("Oulujärvi", "OUJ"),
        new Station("Mekrijärvi", "MEK"),
        new Station("Hankasalmi", "HAN"),
        new Station("Nurmijärvi", "NUR"),
        new Station("Tartto", "TAR")
    );
    private static String currentStation;

    // This is a static class, constructor is private
    private StationsData() {
        // Set Nurmijärvi as default station
        currentStation = stationsList.get(10).name();
    }

    public static List<Station> stations() {
        return stationsList;
    }

    // TODO: refactor into two separate methods, other updates station data and other writes it to prefs
    // Writes the name of the currently selected station to SharedPreferences
    public static void setCurrentStation(@NonNull Context context, String stationName) {
        Log.d("mytag", "IN SETCURRENTSTATION, currenStation: " + currentStation + " stationName: " + stationName);
        currentStation = stationName;

        for (Station station : stationsList) {
            Log.d("mytag", "SETCURRENTSTATION LOOP " + station);
            if (station.name().equals(stationName)) {
                DataMediator.notifySubscribers(station);
            }
        }

        Utils.writeStringToStationStore(context, "current_station_name", stationName);
    }
}
