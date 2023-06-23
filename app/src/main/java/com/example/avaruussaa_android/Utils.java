package com.example.avaruussaa_android;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

// Class for small utility functions.
public class Utils {
    private static final String TAG = "utilstag";

    // Splits a string at <delimiter> and returns the segments in an array.
    public static String[] splitString(@NonNull String string, @NonNull String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter)).split(string);
    }

    // Removes leading and trailing zeroes and useless decimal points from a string.
    public static String trimZeroes(@NonNull String string) {
        string = string.replace(',', '.');

        if (string.equals(".")) {
            return "0";
        }

        // First regex from Kent @ https://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
        string = string.contains(".") ? string.replaceAll("0*$","").replaceAll("\\.$","") : string;
        string = string.length() > 1 ? string.replaceFirst("^0+", "") : string;

        if (string.length() > 0 && string.charAt(0) == '.') {
            string = "0" + string;
        }

        return string.length() > 1 || (string.length() > 0 && string.charAt(0) != '0') ? string : "0";
    }

    // Cuts off strings that are longer than <length> characters.
    public static String truncateString(@NonNull String string, int length) {
        if (string.length() > length) {
           return string.substring(0,length);
        }

        return string;
    }

    // Formats a string representing a decimal numeral by applying the trim and truncate functions to it.
    public static String formatNumberString(@NonNull String string, int length) {
        String formattedString = trimZeroes(string);
        formattedString = truncateString(formattedString, length);
        // Trim again to fix edge cases like "123.04" being truncated to "123.0".
        return trimZeroes(formattedString);
    }

    // Writes a string to a file.
    public static synchronized void writeToFile(@NonNull Context context, @NonNull String filename, String content) {
        Log.d(TAG, "writeToFile: writing to file " + filename);
        try {
            File file = new File(context.getFilesDir(), filename);

            try (FileOutputStream outputStream = new FileOutputStream(file);
                 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
                outputStreamWriter.write(content);
            }
        }
        catch (IOException e) {
            Log.d(TAG, "writeToFile: EXCEPTION: " + e);
        }
    }
}