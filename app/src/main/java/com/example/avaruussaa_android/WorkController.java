package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class WorkController {
    private WorkController() {}

    public static void initWork(@NonNull Context context) {
        Log.d("mytag", "initWork: IN INITWORK");
        WorkRequest request = new OneTimeWorkRequest.Builder(UpdateWorker.class)
//            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // This doesn't work on older Android versions
            .build();

        WorkManager
            .getInstance(context)
            .enqueue(request);

    }
}
