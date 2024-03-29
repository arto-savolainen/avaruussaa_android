package com.example.avaruussaa_android.data.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.avaruussaa_android.utils.Notifier;
import com.example.avaruussaa_android.R;
import com.example.avaruussaa_android.data.StationsData;
import com.example.avaruussaa_android.data.models.Station;
import com.example.avaruussaa_android.utils.AppSettings;
import com.example.avaruussaa_android.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// This Worker fetches weather station data from the internet and sends it to the data store (StationsData).
// It also uses Notifier to send a notification if magnetic activity is above a set threshold.
public class UpdateWorker extends Worker {
    protected static final String TAG = "workertag";
    protected static final String URL = "https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa";

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

        // If data fetching failed, return Result.retry() so that WorkManager will reschedule the worker.
        if (responseBody.length() == 0) {
            setStationsConnectivityError(stationsData);
            Log.d(TAG, "UpdateWorker doWork: DATA FETCH FAILED, retrying...");
            return Result.retry();
        }

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

        // Send collected data to StationsData for caching and saving to SharedPreferences.
        // Main view is also notified and updated, as it's listening to changes in SharedPrefs.
        StationsData.setStationsData(context, stationsData);

        // Send notification if needed.
        checkConditionsAndNotify();

        return Result.success();
    }

    // This function builds a list of stations with the error set to indicate a connectivity problem
    // and sends that list to StationsData, resulting in the error being displayed in the main view.
    private void setStationsConnectivityError(ArrayList<Station> stationsData) {
        Context context = getApplicationContext();

        for (Station station : StationsData.getDefaultStationsList(context)) {
            stationsData.add(createStation(station, "", context.getString(R.string.error_connectivity)));
        }

        StationsData.setStationsData(context, stationsData);
    }

    // Calls Notifier.sendNotification() if conditions regarding the currently selected station's magnetic activity are met.
    // 1) Activity must meet or exceed the threshold set in app settings.
    // 2) Time since the last notification must be less than the notification interval set in app settings.
    // Else calls updateNotification() to silently update an existing notification with new data
    // If notifications are disabled in app settings, returns without doing anything.
    protected void checkConditionsAndNotify() {
        AppSettings settings = new AppSettings();
        Boolean notificationsEnabled = settings.areNotificationsEnabled();

        if (!notificationsEnabled) {
            return;
        }

        Context context = getApplicationContext();
        double notificationThreshold = settings.getNotificationThreshold();
        Station currentStation = StationsData.getCurrentStation(context);
        Boolean silentUpdate = withinInterval();
        double currentActivity = -1;

        try {
            currentActivity = Double.parseDouble(currentStation.activity());
        } catch (NumberFormatException e) {
            Log.e(TAG, "doWork: EXCEPTION: Parsing current station activity to double failed, e: " + e);
        }

        if (currentActivity >= notificationThreshold && !silentUpdate) {
            Boolean notificationSent = Notifier.sendNotification(currentStation.name(), currentStation.activity());

            if (notificationSent) {
                writeLastNotificationTimeToPreferences();
            }
        } else if (currentActivity >= notificationThreshold && silentUpdate) {
            Notifier.updateNotification(currentStation.name(), currentStation.activity());
        }
    }

    // Gets the datetime of the last notification from SharedPreferences and compares it to current time.
    // Returns true if <interval> time has not yet elapsed since the last notification, false if it has.
    protected Boolean withinInterval() {
        long defaultValue = 0;
        long lastNotificationTime = StationsData.getLongFromStationStore(getApplicationContext(), "last_notification_time", defaultValue);

        // If SharedPreferences does not contain the last_notification_time key return false.
        if (lastNotificationTime == defaultValue) {
            Log.d(TAG, "withinInterval: lastNotificationTime == defaultValue, returning false");
            return false;
        }

        AppSettings settings = new AppSettings();
        long intervalMillis = (long) (settings.getNotificationInterval() * 60 * 60 * 1000); // Interval in milliseconds.
        long currentTimeMillis = new Date().getTime(); // Current Unix time in milliseconds.

        Log.d(TAG, "withinInterval: " + currentTimeMillis + " - " + lastNotificationTime + " < " + intervalMillis + " == " + (currentTimeMillis - lastNotificationTime < intervalMillis));
        // If there has not yet elapsed <interval> milliseconds since the last notification, return true.
        return currentTimeMillis - lastNotificationTime < intervalMillis;
    }

    protected void writeLastNotificationTimeToPreferences() {
        long currentTimeMillis = new Date().getTime();
        StationsData.writeLongToStationStore(getApplicationContext(), "last_notification_time", currentTimeMillis);
    }

    protected Station createStation(@NonNull Station station, @NonNull String activity, @NonNull String error) {
        return new Station(station.name(), station.code(), activity, error);
    }

    // Makes a HTTP request to <URL> to retrieve its content and returns the response body as a string.
    protected String fetchData() {
        String responseBody = "";
        int retries = 5;
        int waitSeconds = 1;

        // Sometimes we get an UnknownHostException, which may be caused by phone battery optimizations.
        // In case that happens wait 1 second and retry connection until it succeeds or the retry limit is hit.
        // Probably doesn't help if the OS limits background network traffic but may be useful if it's a server issue.
        for (int i = 1; i <= retries; i++) {
            Log.d(TAG, "fetchData: CONNECTION ATTEMPT NUMBER " + i);
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
                Log.d(TAG, e.toString());

                try {
                    // Sleeping in a worker thread should be fine.
                    Thread.sleep(waitSeconds);
                }
                catch (Exception exception) {
                    Log.d(TAG, "fetchData: EXCEPTION while sleeping, e: " + exception);
                }
            }
        }

        return responseBody; // Return empty string if something went wrong.
    }
}