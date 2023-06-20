package com.example.avaruussaa_android;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class WorkController {
    private static final String TAG = "workcontrollertag";
    private static final long TEN_MINUTES_IN_MILLIS = 10 * 60 * 1000;
    private static final long TIMER_TICK_INTERVAL_MILLIS = 1000;
    private static int PERIODIC_WORK_INTERVAL_MINUTES = 20;
    private static CountDownTimer repeatingWorkEnqueuingTimer;
    private static TimerSubscriber subscriber;

    private WorkController() {}

    public static void startWork() {
        Log.d(TAG, "startWork: WorkController initializing work");

        // Start the initial data fetching operation.
        enqueueSingleWorkRequest();

        // Start the periodic work requests which notifies user of magnetic activity if the app has been killed by the OS.
        startPeriodicWork();

        // Create and start the repeating timer which enqueues new UpdateWorker requests to update station data every 10 minutes.
        startWorkEnqueuingTimer();
    }

    private static void enqueueSingleWorkRequest() {
        Log.d(TAG, "enqueueSingleWorkRequest: building and enqueuing an UpdateWorker request");
        Context context = InitApp.getInstance();

        // Not sure if it's possible to have a situation where our Application instance is gone here but this checks for it.
        if (context == null) {
            return;
        }

        WorkRequest request = new OneTimeWorkRequest.Builder(UpdateWorker.class)
            .build();

        WorkManager
            .getInstance(context)
            .enqueue(request);
    }

    // Enqueues a periodic work request but only if it has not been enqueued before. This runs PeriodicUpdateWorker
    // every 20 minutes to notify the user of changes in magnetic activity in case the app itself has been killed.
    private static void startPeriodicWork() {
        Log.d(TAG, "startPeriodicWork: STARTING PERIODIC WORK!");
        Context context = InitApp.getInstance();

        // Not sure if it's possible to have a situation where our Application instance is gone here but this checks for it.
        if (context == null) {
            return;
        }

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(PeriodicUpdateWorker.class, PERIODIC_WORK_INTERVAL_MINUTES, TimeUnit.MINUTES)
            .build();

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork("PERIODIC_UPDATE_WORKER", ExistingPeriodicWorkPolicy.KEEP, request);
    }

    // Calculates the next time to start the data fetch operation and starts a timer which calls enqueueWorkRequest() at that time.
    // It also calls createRepeatingTimer() to start the repeating timer which enqueues work at 10 minute intervals.
    // Calls subscriber.onTick() every second to notify MainModel, which then updates the timer in the main view.
    private static void startWorkEnqueuingTimer() {
        Calendar currentTime = Calendar.getInstance();
        int currentMinute = currentTime.get(Calendar.MINUTE);
        int currentSecond = currentTime.get(Calendar.SECOND);
        int offsetMinutes = 10 - (currentMinute % 10 == 0 ? 10 : currentMinute % 10);
        int offsetSeconds = 60 - currentSecond;
        long millisUntilUpdate = (long) (offsetMinutes * 60 + offsetSeconds) * 1000;

        Log.d(TAG, "startWorkEnqueuingTimer: minute: " + currentMinute + " second: " + currentSecond + " offsetmin: " + offsetMinutes + " offsetsec: " + offsetSeconds);
        Log.d(TAG, "startWorkEnqueuingTimer: millisUntilUpdate: " + millisUntilUpdate);

        CountDownTimer firstUpdateTimer = new CountDownTimer(millisUntilUpdate, TIMER_TICK_INTERVAL_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                notifySubscriber(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                enqueueSingleWorkRequest();
                createRepeatingTimer();
            }
        }.start();
    }

    private static void notifySubscriber(long millisUntilFinished) {
        if (subscriber != null) {
            subscriber.onTick(millisUntilFinished);
        }
    }

    // Creates the repeating timer which enqueues work requests at a 10 minute interval.
    private static void createRepeatingTimer() {
        repeatingWorkEnqueuingTimer =  new CountDownTimer(TEN_MINUTES_IN_MILLIS, TIMER_TICK_INTERVAL_MILLIS) {
            public void onTick(long millisUntilFinished) {
                notifySubscriber(millisUntilFinished);
            }

            public void onFinish() {
                enqueueSingleWorkRequest();
                this.start(); // Repeat timer until app process is killed.
            }
        }.start();
    }

    public static void subscribe(TimerSubscriber newSubscriber) {
        subscriber = newSubscriber;
    }

    public static void unsubscribe() {
        subscriber = null;
    }
}
