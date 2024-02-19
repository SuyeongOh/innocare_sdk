package com.innopia.vital_sync.utils;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvFileWriter {
    public static void writeCsvFile(Context context, String fileName, List<String[]> dataLines) {
        File file = new File(context.getFilesDir(), fileName);
        FileWriter writer = null;

        try {
            writer = new FileWriter(file, false);

            for (String[] dataLine : dataLines) {
                writer.append(String.join(",", dataLine));
                writer.append("\n");
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
