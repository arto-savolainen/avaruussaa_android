package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;

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

            Log.d(TAG, "STATION DATA: " + stationData);

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

        StationsData.setStationsData(context, stationsData);

        checkConditionsAndNotify();






        return Result.success();
    }

    // Calls Notifier.sendNotification() if conditions regarding the currently selected station's magnetic activity are met.
    // 1) Activity must meet or exceed the threshold set in app settings.
    // 2) Time since the last notification must be less than the notification interval set in app settings.
    private void checkConditionsAndNotify() {
        Context context = getApplicationContext();

        AppSettings settings = new AppSettings();
        Boolean notificationsEnabled = settings.getNotificationsEnabled();
        double notificationThreshold = settings.getNotificationThreshold();
        Station currentStation = StationsData.getCurrentStation(context);

        //TODO: Implement interval check: Write datetime to prefs, check if datetime.now() - lastdatetime > interval -> sendNotification()

        try {
            if (notificationsEnabled && Double.parseDouble(currentStation.activity()) >= notificationThreshold) {
                Notifier.sendNotification(currentStation.name(), currentStation.activity());
            }
        }
        catch (NumberFormatException e) {
            Log.e(TAG, "doWork: EXCEPTION: Parsing current station activity to double failed, e: " + e);
            e.printStackTrace();
        }
    }

    private Station createStation(@NonNull Station station, @NonNull String activity, @NonNull String error) {
        return new Station(station.name(), station.code(), activity, error);
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
                // If response.body() is null return empty string.
                responseBody = response.body() != null ? response.body().string() : "";

                return responseBody;
            }
        } catch (Exception e) {
            Log.d("errortag", e.toString());
        }

        return responseBody; // Return empty string if something went wrong.
    }
}