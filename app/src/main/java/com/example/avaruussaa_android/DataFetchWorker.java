package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataFetchWorker extends Worker {
    private static final String TAG = "mytag";

    @SuppressWarnings("FieldCanBeLocal")
    private final String URL = "https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa";

    public DataFetchWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @WorkerThread
    @Override
    public Result doWork() {
        Log.d(TAG, "DOING WORK");
        String responseBody = fetchData();

        // Find station data from the HTML string and update the activity for each station
        for (Station station : StationsData.stations()) {
            // Station codes are used to find data within the javascript contained in response.body()
            // Data starts after this string
            String delimiter = station.code() + "\\\":{\\\"dataSeries\\\":";

            Log.d("mytag", "DELIMITER: " + delimiter);
            // Split response body in two, the data is found at the beginning of the second array element
            String[] splitResponseBody = Utils.splitString(responseBody, delimiter);

            // Sometimes an observatory's data service may go offline for a while and their information will not be in the javascript
            // If data for this station was not found, splitData.length should equal 1
            // Length is also 1 if the data fetch routine failed and returned an empty string, in which case all stations will be set to an error state
            if (splitResponseBody.length < 2) {
                // Set station error message indicating data was not found and loop to the next station
                station.setError(getApplicationContext().getString(R.string.error_station_disabled, station.name()));
                continue;
            }

            Log.d(TAG, "SPLITRESPONSE LENGTH: " + splitResponseBody.length);
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

            try {
                // Sometimes activity values for stations are recorded as null in the data, in which case set error for the station
                if (activity.contains("null")) {
                    // Use activity previous to latest. This may also be null but most often contains valid data
                    if (previousActivity.contains("null")) {
                        station.setError(getApplicationContext().getString(R.string.error_station_null, station.name()));
                    }
                    else {
                        station.setActivity(Double.parseDouble(previousActivity));
                    }
                }
                else {
                    station.setActivity(Double.parseDouble(activity));
                }
            }
            catch (NumberFormatException e) {
                // This should never be reached if the data is valid
                station.setError(getApplicationContext().getString(R.string.error_station_null, station.name()));
            }

            Log.d(TAG, "STATION AT END OF FOR LOOP: " + station);
        }

        return Result.success();
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
                FileWriter.write(getApplicationContext(), "response.html", responseBody);

                return responseBody;
            }
        } catch (Exception e) {
            Log.d("errortag", e.toString());
        }

        return responseBody; // Define proper error handling later
    }
}
