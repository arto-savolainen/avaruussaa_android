package com.example.avaruussaa_android;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

// Static class for small utility functions
public class Utils {
    public static String[] splitString(@NonNull String splitThis, @NonNull String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter)).split(splitThis);
    }
}