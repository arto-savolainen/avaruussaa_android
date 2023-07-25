package com.example.avaruussaa_android.interfaces;

// Any class which wishes to subscribe to receive updates on each tick of a timer may implement this.
// Used by the MainModel class to follow the timer run by WorkController.
public interface TimerSubscriber {
    void onTick(long millisUntilFinished);
}