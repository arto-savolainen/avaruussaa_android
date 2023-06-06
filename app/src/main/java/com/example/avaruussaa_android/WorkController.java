package com.example.avaruussaa_android;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

public class WorkController {
    private WorkController() {}

    public static void initWork(@NonNull Context context) {
        WorkRequest request = new OneTimeWorkRequest.Builder(DataFetchWorker.class)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build();

        WorkManager
            .getInstance(context)
            .enqueue(request);
    }
}
