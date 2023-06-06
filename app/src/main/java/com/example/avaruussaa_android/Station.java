package com.example.avaruussaa_android;

import androidx.annotation.NonNull;

public class Station {
    private final String name;
    private final String code;
    private double activity;
    private String error;

    // TODO: This constructor can be removed if I don't find a use for it
    public Station(@NonNull String name) {
        this.name = name;
        this.code = "";
    }

    public Station(@NonNull String name, @NonNull String code) {
        this.name = name;
        this.code = code;
    }

    // TODO: This constructor can be removed if I don't find a use for it
    public Station(@NonNull String name, @NonNull String code, int activity) {
        this.name = name;
        this.code = code;
        this.activity = activity;
    }

    public String name() {
        return name;
    }

    public String code() {
        return code;
    }

    public double activity() {
        return activity;
    }

    public void setActivity(double newActivity) {
        activity = newActivity;
        error = null;
    }

    public void setError(String newError) {
        error = newError;
    }

    @NonNull
    @Override
    public String toString() {
        return "{ name: " + name + ", code: " + code + ", activity: " + activity + " error: " + error + " }";
    }
}
