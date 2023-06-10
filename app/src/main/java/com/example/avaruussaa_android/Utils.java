package com.example.avaruussaa_android;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Pattern;

// Class for small utility functions.
public class Utils {
    public static String[] splitString(@NonNull String string, @NonNull String delimiter) {
        return Pattern.compile(Pattern.quote(delimiter)).split(string);
    }

    // Use this to format user input numerals. Removes leading and trailing zeroes.
    public static String removeExtraZeroes(@NonNull String string) {
        if (string.equals(".")) {
            return "0";
        }

        // First regex from https://stackoverflow.com/questions/14984664/remove-trailing-zero-in-java
        string = string.contains(".") ? string.replaceAll("0*$","").replaceAll("\\.$","") : string;
        string = string.length() > 1 ? string.replaceFirst("^0+", "") : string;

        if (string.length() > 0 && string.charAt(0) == '.') {
            string = "0" + string;
        }

        return string.length() > 1 || (string.length() > 0 && string.charAt(0) != '0') ? string : "0";
    }

    public static synchronized void writeToFile(@NonNull Context context, @NonNull String filename, String content) throws IOException {
        File file = new File(context.getFilesDir(), filename);

        try (FileOutputStream outputStream = new FileOutputStream(file);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
            outputStreamWriter.write(content);
        }
    }
}