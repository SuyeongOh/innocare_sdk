package com.inniopia.funnylabs_sdk;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inniopia.funnylabs_sdk.data.VitalChartData;

public class AnalysisActivity extends AppCompatActivity {

    private final double [][] pixelRGB = new double[][]{
            VitalChartData.R_SIGNAL,
            VitalChartData.G_SIGNAL,
            VitalChartData.B_SIGNAL
    };;
    private double [] currentSignal;
    private static final String[] MODULE_ARRAY = new String[]{
            "Smoothing",
            "OMIT",
            "Detrend",
            "Band-Pass Filter",
            "Fast-Fourier Transform",
            "Get Heart Rate"
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

    }

    public double[][] dspSmoothing(double[][] pixel_signal){

    }
    public double[] coreOmit(double[] array){

    }
    public double[] dspDetrend(double[] array){

    }
    public double[] dspBpf(double[] array){

    }
    public double[] dspFft(double[] array){

    }
    public double getHr(double[] array){

    }
}
