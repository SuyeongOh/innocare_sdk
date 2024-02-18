package com.inniopia.innocare_sdk;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.psambit9791.jdsp.signal.Detrend;
import com.github.psambit9791.jdsp.signal.Smooth;
import com.github.psambit9791.jdsp.transform.DiscreteFourier;
import com.inniopia.innocare_sdk.bvp.BandPassFilter;
import com.inniopia.innocare_sdk.bvp.ModuleName;
import com.inniopia.innocare_sdk.data.VitalChartData;

import java.util.ArrayList;
import java.util.List;

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
    private double [] currentSignal;
    private static final String[] MODULE_ARRAY = new String[]{
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

    private double hr;
    private boolean isSmooth;

    private boolean isFFTSignal = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        functionLayout = findViewById(R.id.analysis_button_group);
        analysisChart = findViewById(R.id.analysis_chart_view);
        processTextView = findViewById(R.id.analysis_text_progress);
        resetButton = findViewById(R.id.analysis_button_reset);

        init();
        initChart("description");

        drawChart(pixelRGB[0], Color.RED);
        drawChart(pixelRGB[1], Color.GREEN);
        drawChart(pixelRGB[2], Color.BLUE);
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
           drawChart(smoothPixel[0], Color.RED);
           drawChart(smoothPixel[1], Color.GREEN);
           drawChart(smoothPixel[2], Color.BLUE);
           initButtonGroup();
        });
        Button buttonNo = new Button(this);
        buttonNo.setLayoutParams(functionLayoutParams);
        buttonNo.setText("아니오");
        buttonNo.setOnClickListener(view -> {
            initButtonGroup();
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
        BandPassFilter bpf = new BandPassFilter(Config.MAX_RR_FREQUENCY, Config.MIN_HR_FREQUENCY);

        double[] bpf_signal = new double[signal.length];
        for(int i = 1; i < signal.length; i++){
            bpf_signal[i] = bpf.filter(signal[i], VitalChartData.frameTimeArray[i] - VitalChartData.frameTimeArray[i - 1]);
        }
        return bpf_signal;
    }
    public double[] dspFft(double[] signal){
        DiscreteFourier fft_r = new DiscreteFourier(signal);
        fft_r.transform();

        return fft_r.getMagnitude(true);
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

    private void initButtonGroup(){
        for (String buttonText : MODULE_ARRAY) {
            Button button = new Button(this);

            button.setLayoutParams(functionLayoutParams);
            button.setText(buttonText);
            button.setOnClickListener(view -> {
                double[][] targetSignal = new double[3][];
                if(isSmooth){
                    targetSignal = smoothPixel;
                }else{
                    targetSignal = pixelRGB;
                }
                switch (buttonText){
                    case ModuleName.CORE_OMIT:
                        currentSignal = coreOmit(targetSignal);
                        drawChart(currentSignal, Color.BLUE);
                        break;
                    case ModuleName.DSP_DETREND:
                        currentSignal = dspDetrend(currentSignal);
                        drawChart(currentSignal, Color.BLUE);
                        break;
                    case ModuleName.DSP_BPF:
                        currentSignal = dspBpf(currentSignal);
                        drawChart(currentSignal, Color.BLUE);
                        break;
                    case ModuleName.DSP_FFT:
                        currentSignal = dspFft(currentSignal);
                        isFFTSignal = true;
                        drawChart(currentSignal, Color.BLUE);
                        break;
                    case ModuleName.VITAL_HR:
                        if(isFFTSignal){
                            hr = getHr(currentSignal);
                            drawChart(currentSignal, Color.BLUE);
                        }
                        break;
                }
            });
            functionLayout.addView(button);
        }
    }
    private void initChart(String description){
        analysisChart.getDescription().setText(description);
        analysisChart.getDescription().setEnabled(true);
        Legend legend = analysisChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10);
        legend.setTextSize(13);
        legend.setTextColor(Color.parseColor("#A3A3A3"));
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        legend.setYEntrySpace(3);

        XAxis xAxis = analysisChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 데이터 표시 위치
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setSpaceMin(0.1f); // Chart 맨 왼쪽 간격 띄우기
        xAxis.setSpaceMax(0.1f); // Chart 맨 오른쪽 간격 띄우기

        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxisLeft = analysisChart.getAxisLeft();
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setTextColor(Color.BLACK);
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.resetAxisMinimum();
        yAxisLeft.setAxisLineWidth(2);

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxis = analysisChart.getAxisRight();
        yAxis.setDrawLabels(false); // label 삭제
        yAxis.setTextColor(Color.BLACK);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisLineWidth(2);
        yAxisLeft.resetAxisMinimum();
        yAxis.setAxisMaximum((float) 1); // 최댓값
        yAxis.setGranularity((float) 0.1);
    }
    private void drawChart(double[] signal, int lineColor){
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < signal.length; i++) {
            entryList.add(new Entry(i, (float) signal[i]));
        }
        LineDataSet dataset = new LineDataSet(entryList, "");
        dataset.setDrawCircles(false);
        dataset.setColor(lineColor);
        LineData data = new LineData(dataset);
        analysisChart.setData(data);
        analysisChart.notifyDataSetChanged();
        analysisChart.invalidate();
    }
    private void drawHrChart(double[] signal, int lineColor){
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < signal.length; i++) {
            entryList.add(
                    new Entry(VitalChartData.FREQUENCY_INTERVAL * 60 * (i + VitalChartData.START_FILTER_INDEX), (float) signal[i]));
        }
        LineDataSet dataset = new LineDataSet(entryList, "");
        dataset.setDrawCircles(false);
        dataset.setColor(lineColor);
        LineData data = new LineData(dataset);
        analysisChart.setData(data);
        analysisChart.notifyDataSetChanged();
        analysisChart.invalidate();
    }
    private final ViewGroup.LayoutParams functionLayoutParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
}
