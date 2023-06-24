package com.example.avaruussaa_android;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkController {
    private static final String TAG = "workcontrollertag";
    private static final long TIMER_TICK_INTERVAL_MILLIS = 1000;
    private static final int PERIODIC_WORK_INTERVAL_MINUTES = 15;
    private static CountDownTimer workSchedulerTimer;
    private static TimerSubscriber subscriber;

    private WorkController() {}

    public static void startWork() {
        Log.d(TAG, "startWork: WorkController initializing work");

        // Start the initial data fetching operation.
        enqueueWorkRequest();

        // Start the periodic work requests which notifies user of magnetic activity if the app has been killed by the OS.
        startPeriodicWork();

        // Create and start the repeating timer which enqueues new UpdateWorker requests to update station data every 10 minutes.
        Log.d(TAG, "startWorkEnqueuingTimer: Creating the first timer!");
        workSchedulerTimer = createTimer(calculateTimerDuration());
    }

    private static void enqueueWorkRequest() {
        Log.d(TAG, "enqueueSingleWorkRequest: building and enqueuing an UpdateWorker request");
        Context context = InitApp.getInstance();

        // Not sure if it's possible to have a situation where our Application instance is gone here but this checks for it.
        if (context == null) {
            return;
        }

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UpdateWorker.class)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS)
            .build();

        WorkManager.getInstance(context).enqueueUniqueWork("UPDATE_WORKER", ExistingWorkPolicy.REPLACE, request);
    }

    // Enqueues a periodic work request but only if it has not been scheduled before. This runs PeriodicUpdateWorker
    // every 15 minutes to notify the user of changes in magnetic activity in case the app itself has been killed.
    private static void startPeriodicWork() {
        Log.d(TAG, "startPeriodicWork: STARTING PERIODIC WORK!");
        Context context = InitApp.getInstance();

        // Not sure if it's possible to have a situation where our Application instance is gone here but this checks for it.
        if (context == null) {
            return;
        }

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(PeriodicUpdateWorker.class, PERIODIC_WORK_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .setInitialDelay(calculateTimerDuration() + 30 * 1000, TimeUnit.MILLISECONDS)
            .build();

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("PERIODIC_UPDATE_WORKER", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request);
    }

    // This function calculates how many milliseconds from the current moment to the next time data should be updated.
    // The remote data source updates every ten minutes, e.g. at 12.00, 12.10, 12.20, etc. We choose to fetch data
    // one minute after the source has updated (since there are occasionally delays), so at 12.01, 12.11, 12.21, and so on.
    private static long calculateTimerDuration() {
        Calendar currentTime = Calendar.getInstance();
        int currentMinute = currentTime.get(Calendar.MINUTE);
        int currentSecond = currentTime.get(Calendar.SECOND);
        int offsetMinutes = 10 - (currentMinute % 10 == 0 ? 10 : currentMinute % 10);
        int offsetSeconds = 60 - currentSecond;

        Log.d(TAG, "calculateTimerDuration: minute: " + currentMinute + " second: " + currentSecond + " offsetmin: " + offsetMinutes + " offsetsec: " + offsetSeconds);

        return (long) (offsetMinutes * 60 + offsetSeconds) * 1000;
    }

    // Creates and starts a timer and returns a reference to it. When the timer finishes it calls enqueueSingleWorkRequest()
    // and starts another timer, thus keeping the data fetching loop going until the app is killed.
    private static CountDownTimer createTimer(long durationMillis) {
        Log.d(TAG, "createTimer: Creating new timer with duration: " + durationMillis / 1000 + " seconds");
        return new CountDownTimer(durationMillis, TIMER_TICK_INTERVAL_MILLIS) {
            public void onTick(long millisUntilFinished) {
                notifySubscriber(millisUntilFinished);
            }

            public void onFinish() {
                Log.d(TAG, "UPDATE TIMER onFinish() reached");
                enqueueWorkRequest();
                workSchedulerTimer = createTimer(calculateTimerDuration());
            }
        }.start();
    }

    private static void notifySubscriber(long millisUntilFinished) {
        if (subscriber != null) {
            subscriber.onTick(millisUntilFinished);
        }
    }

    // This can be used to manually schedule an immediate data re-fetch.
    public static void triggerUpdate() {
        enqueueWorkRequest();
    }

    public static void subscribe(TimerSubscriber newSubscriber) {
        subscriber = newSubscriber;
    }

    public static void unsubscribe() {
        subscriber = null;
    }
}
