package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.WorkerParameters;

import java.util.ArrayList;

// This class is the same as UpdateWorker, except it only does work if InitApp.getInstance() returns null.
// That is, if our app has been killed or Application does not exist for some other reason.
public class PeriodicUpdateWorker extends UpdateWorker {
    protected static final String TAG = "periodicworkertag";

    public PeriodicUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @WorkerThread
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        Log.d(TAG, "!!!!! PERIODIC !!!!! WORKER ENTERED doWork(). context: " + context);

        // Only do work with the periodic worker if the app has been killed or not yet started after device reboot.
        // Otherwise data fetching is done by UpdateWorker which is scheduled to run every 10 minutes by WorkController.
        if (InitApp.getInstance() != null) {
            return Result.success();
        }

        Log.d(TAG, "--------------------- PERIODIC WORKER DOING WORK ---------------------");
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
