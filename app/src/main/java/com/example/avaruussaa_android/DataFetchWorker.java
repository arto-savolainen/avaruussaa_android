package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataFetchWorker extends Worker {
    private final String URL = "https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa";

    public DataFetchWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("mytag", "DOING WORK");
        String responseBody = fetchDataString();



        // Find station data from the html string and update the activity for each station
        for(Station station : StationsData.stations()) {
            // Station codes are used to find data within the javascript contained in response.body()
            // Data starts after this string
            final String delimiter = station.code() + "\\\"dataSeries\\\":";
            String[] splitData = responseBody.split(delimiter);

            // If data for this station was not found, splitData.length should equal 1
            if (splitData.length < 2) {

            }


        }


        return Result.success();
    }

    private String fetchDataString() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                .url(URL)
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Expires", "0")
                .build();

            try (Response response = client.newCall(request).execute()) {
                // Write response body to a file for debugging purposes
                FileWriter.write(getApplicationContext(), "response.html", response.body().string());
                return response.body().string();
            }
        } catch (Exception e) {
            Log.d("errortag", e.toString());
        }

        return "error"; // Define proper error handling later
    }
}
