package com.innopia.innocare_sdk;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModelProvider;

public class SampleApp extends Application {
    public static ViewModelProvider.AndroidViewModelFactory factory = null;
    public static Context context;

    public SampleApp() {
        factory = new ViewModelProvider.AndroidViewModelFactory(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
    }
}
