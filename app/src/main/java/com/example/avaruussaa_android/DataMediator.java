package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

public class DataMediator {
    private static MainModel subscriber;

    private DataMediator() {}

    public static void setCurrentStation(@NonNull Context context, String stationName) {
        StationsData.setCurrentStation(context, stationName);
    }

    public static void notifySubscribers(Station station) {
        Log.d("mytag", "IN NOTIFYSUBSCRIBERS, station: " + station + " subscriber: " + subscriber);
        if (subscriber != null) {
            subscriber.updateModelData(station);
        }
    }

    public static void subscribe(MainModel newSubscriber) {
        subscriber = newSubscriber;
    }

    public static void unsubscribe() {
        subscriber = null;
    }
}
