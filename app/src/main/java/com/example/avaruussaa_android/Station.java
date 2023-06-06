package com.example.avaruussaa_android;

import androidx.annotation.NonNull;

public class Station {
    private String name;
    private String code;
    private int activity;
    private String error;

    // This constructor can be removed if I don't find a use for it
    public Station(@NonNull String name) {
        this.name = name;
        this.code = "";
    }

    public Station(@NonNull String name, @NonNull String code) {
        this.name = name;
        this.code = code;
    }

    // This constructor can be removed if I don't find a use for it
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

    public int activity() {
        return activity;
    }

    public void setActivity(@NonNull int newActivity) {
        activity = newActivity;
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
