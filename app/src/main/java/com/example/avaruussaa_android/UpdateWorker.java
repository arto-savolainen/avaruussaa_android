package com.example.avaruussaa_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateWorker extends Worker {
    private static final String TAG = "workertag";
    private static final String URL = "https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa";

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }


    @NonNull
    @WorkerThread
    @Override
    public Result doWork() {
        Log.d(TAG, "DOING WORK");
        String responseBody = fetchData();
        Context context = getApplicationContext();

        //TODO: FIGURE OUT APP LIFECYCLES AND ONLY ACCESS STATIONSDATA IF ITS ACTUALLY ALIVE AND HAS DATA
        // not sure if it's possible for the data in StationData to be destroyed. Probably is
        // in that case, create a separate for loop which only handles notifications

        // Find station data from the HTML string and update the activity for each station
        for (Station station : StationsData.getDefaultStationList(context)) {
            // Station codes are used to find data within the javascript contained in response.body()
            // Data starts after this string
            String delimiter = station.code() + "\\\":{\\\"dataSeries\\\":";
            // Split response body in two, the data is found at the beginning of the second array element
            String[] splitResponseBody = Utils.splitString(responseBody, delimiter);

            // Sometimes an observatory's data service may go offline for a while and their information will not be in the javascript
            // If data for this station was not found, splitData.length should equal 1
            // Length is also 1 if the data fetch routine failed and returned an empty string, in which case all stations will be set to an error state
            if (splitResponseBody.length < 2) {
                // Set station error message indicating data was not found and loop to the next station
                station.setError(context.getString(R.string.error_station_disabled, station.name()));
                continue;
            }

            // The data we're looking for ends with "}"
            // Now the response body will be cut with the starting and ending delimiters to obtain the javascript array holding station data
            splitResponseBody = Utils.splitString(splitResponseBody[1], "}");
            String stationData = splitResponseBody[0];

            Log.d(TAG, "STATION DATA: " + stationData);

            // We further split the string to obtain the last element in the javascript array
            // The last element is the latest recorded activity value for the station, so we remove trailing brackets
            // We record the activity of the second last element as well in case the latest data is null
            String[] splitData = Utils.splitString(stationData, ",");
            String activity = Utils.splitString(splitData[splitData.length - 1], "]")[0];
            String previousActivity = Utils.splitString(splitData[splitData.length - 3], "]")[0];

            // Sometimes activity values for stations are recorded as null in the data, in which case set error for the station
            if (activity.contains("null")) {
                // If activity was null, use activity previous to latest. This may also be null but most often contains valid data
                if (previousActivity.contains("null")) {
                    setError(station, getApplicationContext().getString(R.string.error_station_null, station.name()));
                } else {
                    setActivity(station, previousActivity);
                }
            } else {
                setActivity(station, activity);
            }

            Log.d(TAG, "STATION AT END OF FOR LOOP: " + station);
        }

        Log.d(TAG, "doWork: end of station loop");
        // Write a new value to key "refresh" in SharedPreferences in order to trigger onSharedPreferencesChange() in MainModel
        Log.d(TAG, "VALUE OF REFRESH: " + StationsData.getRefreshFromPreferences(context));
        StationsData.refreshSharedPreferences(context);

        Notifier.sendNotification(context,"paskastation", "420");

        return Result.success();
    }

    private void setActivity(Station station, String activity) {
        station.setActivity(activity);
        StationsData.setStationData(getApplicationContext(), station);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        settings.getString("treshold", "");
    }

    private void setError(Station station, String error) {
        station.setError(error);
        StationsData.setStationData(getApplicationContext(), station);
    }

    private String fetchData() {
        String responseBody = "";

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                .url(URL)
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Expires", "0")
                .build();

            try (Response response = client.newCall(request).execute()) {
                // If response.body() is null return empty string
                responseBody = response.body() != null ? response.body().string() : "";

                // Write response body to a file for debugging purposes
                Utils.writeToFile(getApplicationContext(), "response.html", responseBody);

                return responseBody;
            }
        } catch (Exception e) {
            Log.d("errortag", e.toString());
        }

        return responseBody; // Return empty string if something went wrong
    }
}
