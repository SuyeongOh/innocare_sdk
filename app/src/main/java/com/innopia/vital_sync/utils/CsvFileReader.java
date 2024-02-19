package com.innopia.vital_sync.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvFileReader {
    public static List<String[]> readCsvFile(Context context, String fileName) {
        List<String[]> resultList = new ArrayList<>();
        File file = new File(context.getFilesDir(), fileName);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                // CSV 파일의 각 줄을 쉼표로 분리하여 배열로 변환
                resultList.add(tokens);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultList;
    }

    public static List<String[]> readCsvFromAssets(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        List<String[]> resultList = new ArrayList<>();

        try (InputStream is = assetManager.open(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;

            while ((line = reader.readLine()) != null) {
                // CSV 파일의 각 줄을 쉼표로 분리하여 배열로 변환
                String[] tokens = line.split(",");
                resultList.add(tokens);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultList;
    }

}
