package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class WorkController {
    private WorkController() {}

    public static void initWork(@NonNull Context context) {
        //TODO: create a periodic work request AND run a timer which triggers a work request at 10 min intervals
        // Create a timer class to encapsulate the 10 min repeating timeout, with start() stop() etc.
        // Modify UpdateWorker so the periodic worker only sends notifications and doesn't update data. add parameters with sendInputData

        // TODO: Constrain the timed work requests, or at least the periodic one, if on a metered connection
        Log.d("mytag", "initWork: IN INITWORK");
        WorkRequest request = new OneTimeWorkRequest.Builder(UpdateWorker.class)
//            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // This doesn't work on older Android versions
            .build();

        WorkManager
            .getInstance(context)
            .enqueue(request);

    }
}
