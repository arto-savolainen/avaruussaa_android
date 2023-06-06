package com.example.avaruussaa_android;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

// A simple file writer that writes a string to a file located in data/data/%appfolder%/files
public class FileWriter {
    // This is a static class, constructor is private
    private FileWriter() {
    }

    public static void write(@NonNull Context context, @NonNull String filename, @NonNull String content) throws IOException {
        File file = new File(context.getFilesDir(), filename);

        try (FileOutputStream outputStream = new FileOutputStream(file);
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream)) {
            outputStreamWriter.write(content);
        }
    }
}