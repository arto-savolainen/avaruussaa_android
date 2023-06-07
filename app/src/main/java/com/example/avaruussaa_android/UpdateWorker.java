package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateWorker extends Worker {
    private static final String TAG = "mytag";
    private static final String URL = "https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa";

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    //TODO: CHECK APP LIFECYCLES AND ONLY ACCESS STATIONSDATA IF ITS ACTUALLY ALIVE AND HAS DATA

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
                // Use activity previous to latest. This may also be null but most often contains valid data
                if (previousActivity.contains("null")) {
                    station.setError(getApplicationContext().getString(R.string.error_station_null, station.name()));
                } else {
                    setActivity(station, previousActivity);
                }
            } else {
                setActivity(station, activity);
            }

            Log.d(TAG, "STATION AT END OF FOR LOOP: " + station);
        }

        return Result.success();
    }

    // Sets station activity in StationsData. Also checks if the station whose data we are changing is the currently selected station
    // in which case station data is also written to SharedPreferences
    private void setActivity(Station station, String activity) {
        station.setActivity(activity);

        String currentStationName = Utils.getStringFromSharedPreferences(getApplicationContext(), "current_station_name");

        if (station.name().equals(currentStationName)) {
            StationsData.setCurrentStation(getApplicationContext(), station.name());
        }
    }

    // Sets station error in StationsData. Also checks if the station whose data we are changing is the currently selected station
    // in which case calls StationData.updateCurrentStation()
    private void setError(Station station, String error) {
        station.setActivity(error);

        String currentStationName = Utils.getStringFromSharedPreferences(getApplicationContext(), "current_station_name");

        if (Objects.equals(station.name(), currentStationName)) {
            List<Pair<String, String>> stationData = createStationPreferenceList(station.name(), "", error);
            Utils.writeStringsToStationStore(getApplicationContext(), stationData);
        }
    }

    private List<Pair<String, String>> createStationPreferenceList(String name, String activity, String error) {
        return List.of(
            new Pair<>("current_station_name", name),
            new Pair<>("current_station_activity", activity),
            new Pair<>("current_station_error", error)
        );
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
