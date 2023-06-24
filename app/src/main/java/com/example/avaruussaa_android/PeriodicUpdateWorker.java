package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.Date;

// This class is the same as UpdateWorker except it only fetches data if the cache hasn't been updated in a while.
// Note: I've found that many phones, depending on the manufacturer, will slow or stop timers when the app is in
// the background to save battery. Thus periodic workers are the only way to reliably send notifications.
public class PeriodicUpdateWorker extends UpdateWorker {
    protected static final String TAG = "periodicworkertag";
    private static final long CACHE_MAX_AGE_MILLIS = 10 * 60 * 1000; // Cache should be no older than 10 minutes.

    public PeriodicUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @WorkerThread
    @Override
    public Result doWork() {
        Log.d(TAG, "!!!!! PERIODIC !!!!! WORKER ENTERED doWork()");
        // The purpose of this Worker is to trigger notifications, so exit immediately if notifications are disabled.
        AppSettings settings = new AppSettings();
        boolean notificationsEnabled = settings.areNotificationsEnabled();
        if (!notificationsEnabled) {
            return Result.success();
        }

        Context context = getApplicationContext();
        long lastUpdateTimeMillis = StationsData.getLongFromStationStore(context, "last_update_time", 0);
        long currentTimeMillis = new Date().getTime();

        Log.d(TAG, "doWork: currentTime " + currentTimeMillis + " - lastUpdateTime " + lastUpdateTimeMillis + " < cacheMaxAge " + CACHE_MAX_AGE_MILLIS + " == " + (currentTimeMillis - lastUpdateTimeMillis < CACHE_MAX_AGE_MILLIS));

        // If data was updated less than cacheMaxAge ago, no need to fetch it from the web. We notify using cached data and finish the Worker.
        if (currentTimeMillis - lastUpdateTimeMillis < CACHE_MAX_AGE_MILLIS) {
            checkConditionsAndNotify();
            return Result.success();
        }

        String responseBody = fetchData();
        ArrayList<Station> stationsData = new ArrayList<>();

        // Find station data from the HTML string and update the activity for each station.
        for (Station station : StationsData.getDefaultStationsList(context)) {
            // Station codes are used to find data within the javascript contained in response.body().
            // Data starts after this string.
            String delimiter = station.code() + "\\\":{\\\"dataSeries\\\":";
            // Split response body in two, the data is found at the beginning of the second array element.
            String[] splitResponseBody = Utils.splitString(responseBody, delimiter);

            // Sometimes an observatory's data service may go offline for a while and their information will not be in the javascript.
            // If data for this station was not found, splitData.length should equal 1.
            // Length is also 1 if the data fetch routine failed and returned an empty string, in which case all stations will be set to an error state.
            if (splitResponseBody.length < 2) {
                // Set station error message indicating data was not found and loop to the next station.
                stationsData.add(createStation(station, "", context.getString(R.string.error_station_disabled, station.name())));
                continue;
            }

            // The data we're looking for ends with "}".
            // Now the response body will be cut with the starting and ending delimiters to obtain the javascript array holding station data.
            splitResponseBody = Utils.splitString(splitResponseBody[1], "}");
            String stationData = splitResponseBody[0];

            // We further split the string to obtain the last element in the javascript array.
            // The last element is the latest recorded activity value for the station, so we remove trailing brackets.
            // We record the activity of the second last element as well in case the latest data is null.
            String[] splitData = Utils.splitString(stationData, ",");
            String activity = Utils.splitString(splitData[splitData.length - 1], "]")[0];
            String previousActivity = Utils.splitString(splitData[splitData.length - 3], "]")[0];

            // Sometimes activity values for stations are recorded as null in the data, in which case set error for the station.
            if (activity.contains("null")) {
                // If activity was null, use activity previous to latest. This may also be null but most often contains valid data.
                if (previousActivity.contains("null")) {
                    stationsData.add(createStation(station, "", context.getString(R.string.error_station_null, station.name())));
                } else {
                    stationsData.add(createStation(station, previousActivity, ""));
                }
            } else {
                stationsData.add(createStation(station, activity, ""));
            }
        }

        Log.d(TAG, "doWork: end of loop, stationsData:" + stationsData);

        // Send collected data to StationsData for caching and saving to SharedPreferences.
        StationsData.setStationsData(context, stationsData);

        // Send notification if needed.
        checkConditionsAndNotify();

        return Result.success();
    }
}
