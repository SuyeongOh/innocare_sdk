package com.inniopia.funnylabs_sdk;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.psambit9791.jdsp.signal.Detrend;
import com.github.psambit9791.jdsp.signal.Smooth;
import com.github.psambit9791.jdsp.transform.DiscreteFourier;
import com.inniopia.funnylabs_sdk.bvp.BandPassFilter;
import com.inniopia.funnylabs_sdk.data.VitalChartData;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import jsat.linear.DenseMatrix;
import jsat.linear.DenseVector;
import jsat.linear.Matrix;
import jsat.linear.Vec;

public class AnalysisActivity extends AppCompatActivity {

    private final int DETREND_POWER = 6;
    private final double [][] pixelRGB = new double[][]{
            VitalChartData.R_SIGNAL,
            VitalChartData.G_SIGNAL,
            VitalChartData.B_SIGNAL
    };
    private double [][] smoothPixel = new double[3][];
    private double [] smoothR;
    private double [] smoothG;
    private double [] smoothB;
    private double [] currentSignal;
    private static final String[] MODULE_ARRAY = new String[]{
            "Smoothing",
            "OMIT",
            "Detrend",
            "Band-Pass Filter",
            "Fast-Fourier Transform",
            "Get Heart Rate"
    };

    private LinearLayout functionLayout;
    private LineChart analysisChart;
    private TextView processTextView;
    private Button resetButton;

    private boolean isSmooth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        functionLayout = findViewById(R.id.analysis_button_group);
        analysisChart = findViewById(R.id.analysis_chart_view);
        processTextView = findViewById(R.id.analysis_text_progress);
        resetButton = findViewById(R.id.analysis_button_reset);


    }

    private void init(){
        TextView textView = new TextView(this);
        textView.setLayoutParams(functionLayoutParams);
        textView.setText("Smooting 진행 하시겠습니까?");
        Button buttonYes = new Button(this);
        buttonYes.setLayoutParams(functionLayoutParams);
        buttonYes.setText("예");
        buttonYes.setOnClickListener(view -> {
           smoothPixel = dspSmoothing(pixelRGB);
        });
        Button buttonNo = new Button(this);
        buttonNo.setLayoutParams(functionLayoutParams);
        buttonNo.setText("아니오");
        buttonNo.setOnClickListener(view -> {

        });
        functionLayout.addView(buttonYes);
    }
    public double[][] dspSmoothing(double[][] pixel_signal){
        double[][] smooth = new double[3][];
        smooth[0] = new Smooth(pixel_signal[0], 4, "rectangular").smoothSignal();
        smooth[1] = new Smooth(pixel_signal[1], 4, "rectangular").smoothSignal();
        smooth[2] = new Smooth(pixel_signal[2], 4, "rectangular").smoothSignal();

        return smooth;
    }
    public double[] coreOmit(double[][] signalRGB){
        Vec v = new DenseVector(1);
        v.mutableAdd(1);

        DenseMatrix signal = new DenseMatrix(signalRGB);

        Matrix[] qr = signal.qr();
        Matrix q = qr[0];

        Vec sVec = q.getColumn(0);

        DenseMatrix S = new DenseMatrix(v, sVec);

        DenseMatrix identify = DenseMatrix.eye(3);
        Matrix P = identify.subtract(S.transpose().multiply(S));

        Matrix Y = P.multiply(signal);

        Vec bvpVec = Y.getRow(1);

        return bvpVec.arrayCopy();
    }
    public double[] dspDetrend(double[] array){
        Detrend det = new Detrend(array, DETREND_POWER);
        return det.detrendSignal();
    }
    public double[] dspBpf(double[] signal){
        BandPassFilter bpf = new BandPassFilter(Config.MAX_FREQUENCY, Config.MIN_FREQUENCY);

        double[] bpf_signal = new double[signal.length];
        for(int i = 1; i < signal.length; i++){
            bpf_signal[i] = bpf.filter(signal[i], VitalChartData.frameTimeArray[i] - VitalChartData.frameTimeArray[i - 1]);
        }
        return bpf_signal;
    }
    public double[] dspFft(double[] signal){
        DiscreteFourier fft_r = new DiscreteFourier(signal);
        fft_r.dft();

        return fft_r.returnAbsolute(true);
    }
    public double getHr(double[] signalFFT){
        int frame = VitalLagacy.VIDEO_FRAME_RATE;
        ArrayList<Double> hr_signal = new ArrayList<>();
        int max_index = 0;
        float max_val = 0;
        float frequency_interval = VitalLagacy.VIDEO_FRAME_RATE / (float)(signalFFT.length * 2);
        VitalChartData.FREQUENCY_INTERVAL = frequency_interval;
        VitalChartData.FRAME_RATE = frame;
        Log.d("Juptier", "frame rate : " + frame);
        for( int i =0 ; i < signalFFT.length ; i++){
            if(i * frequency_interval < 0.75)
                continue;
            else if( i * frequency_interval > 2.5){
                Log.d("Juptier", "last filter index : " + i);
                break;
            } else{
                if(VitalChartData.START_FILTER_INDEX == 0){
                    VitalChartData.START_FILTER_INDEX = i;
                    Log.d("Juptier", "start filter index : " + i);
                }
                hr_signal.add(signalFFT[i]);
                if( signalFFT[i] > max_val ) {
                    max_val = (float) signalFFT[i];
                    max_index = i;
                }
            }
        }

        VitalChartData.HR_SIGNAL = new double[hr_signal.size()];
        for(int i = 0; i < hr_signal.size(); i++){
            VitalChartData.HR_SIGNAL[i] = hr_signal.get(i);
        }
        return max_index * frequency_interval * 60;
    }

    private final ViewGroup.LayoutParams functionLayoutParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
}
