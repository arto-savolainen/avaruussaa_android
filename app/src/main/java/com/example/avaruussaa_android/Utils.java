package com.example.avaruussaa_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Pattern;

// Static class for small utility functions
public class Utils {
    public static String[] splitString(@NonNull String string, @NonNull String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter)).split(string);
    }

    // Use this to format user input numerals
    public static void removeTrailingZeroes(@NonNull String string) {
        // TODO implementation
    }

    public static void writeToFile(@NonNull Context context, @NonNull String filename, String content) throws IOException {
        File file = new File(context.getFilesDir(), filename);

        try (FileOutputStream outputStream = new FileOutputStream(file);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
            outputStreamWriter.write(content);
        }
    }
}