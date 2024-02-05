package com.inniopia.funnylabs_sdk;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.inniopia.funnylabs_sdk.data.ResultVitalSign;
import com.inniopia.funnylabs_sdk.data.VitalChartData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private LineChart greenChart;
    private LineChart smoothChart;
    private LineChart coreChart;
    private LineChart detrendChart;
    private LineChart bpfChart;
    private LineChart fftChart;
    private LineChart bvpChart;
    private LineChart hrChart;

    private Button restartBtn;
    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        restartBtn = findViewById(R.id.result_recheck_btn);
        restartBtn.setText(String.format("재검사, frame : %d", VitalChartData.FRAME_RATE));
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VitalChartData.START_FILTER_INDEX = 0;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });
        bindChart();
        setValue();
    }

    private void bindChart(){
        greenChart = findViewById(R.id.g_chart);
        smoothChart = findViewById(R.id.smooth_chart);
        coreChart = findViewById(R.id.core_chart);
        detrendChart = findViewById(R.id.detrend_chart);
        bpfChart = findViewById(R.id.bpf_chart);
        bvpChart = findViewById(R.id.bvp_chart);
        fftChart = findViewById(R.id.fft_chart);
        hrChart = findViewById(R.id.hr_chart);

        initChart(greenChart, "G Signal");
        initChart(smoothChart, "Smooth");
        initChart(coreChart, "Core");
        initChart(detrendChart, "Detrend");
        initChart(bpfChart, "BPF");
        initChart(bvpChart, "BVP");
        initChart(fftChart, "FFT");
        initChart(hrChart, "FFT-HR");

        setLineChart();
    }

    @SuppressLint("SetTextI18n")
    private void setValue(){
        ResultVitalSign vitalsign = ResultVitalSign.vitalSignData;
    }

    private void setLineChart(){
        drawChart(greenChart, VitalChartData.R_SIGNAL, Color.RED);
        drawChart(greenChart, VitalChartData.G_SIGNAL, Color.GREEN);
        drawChart(greenChart, VitalChartData.B_SIGNAL, Color.BLUE);
        drawChart(smoothChart, VitalChartData.SMOOTH_R_SIGNAL, Color.RED);
        drawChart(smoothChart, VitalChartData.SMOOTH_G_SIGNAL, Color.GREEN);
        drawChart(smoothChart, VitalChartData.SMOOTH_B_SIGNAL, Color.BLUE);
        drawChart(coreChart, VitalChartData.CORE_SIGNAL, Color.MAGENTA);
        drawChart(detrendChart, VitalChartData.DETREND_SIGNAL, Color.CYAN);
        drawChart(bpfChart, VitalChartData.BPF_SIGNAL, Color.BLUE);
        drawChart(bvpChart, VitalChartData.BVP_SIGNAL, Color.MAGENTA);
        drawChart(fftChart, VitalChartData.FFT_SIGNAL, Color.CYAN);
        drawHrChart(hrChart, VitalChartData.HR_SIGNAL, Color.BLUE);
    }

    private void initChart(LineChart chart, String description){
        chart.getDescription().setText(description);
        chart.getDescription().setEnabled(true);
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10);
        legend.setTextSize(13);
        legend.setTextColor(Color.parseColor("#A3A3A3"));
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        legend.setYEntrySpace(3);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 데이터 표시 위치
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setSpaceMin(0.1f); // Chart 맨 왼쪽 간격 띄우기
        xAxis.setSpaceMax(0.1f); // Chart 맨 오른쪽 간격 띄우기

        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setTextColor(Color.BLACK);
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.resetAxisMinimum();
        yAxisLeft.setAxisLineWidth(2);

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxis = chart.getAxisRight();
        yAxis.setDrawLabels(false); // label 삭제
        yAxis.setTextColor(Color.BLACK);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisLineWidth(2);
        yAxisLeft.resetAxisMinimum();
        yAxis.setAxisMaximum((float) 1); // 최댓값
        yAxis.setGranularity((float) 0.1);
    }

    private void drawChart(LineChart chart, double[] signal, int lineColor){
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < signal.length; i++) {
            entryList.add(new Entry(i, (float) signal[i]));
        }
        LineDataSet dataset = new LineDataSet(entryList, "");
        dataset.setDrawCircles(false);
        dataset.setColor(lineColor);
        LineData data = new LineData(dataset);
        chart.setData(data);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
    private void drawHrChart(LineChart chart, double[] signal, int lineColor){
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < signal.length; i++) {
            entryList.add(
                    new Entry(VitalChartData.FREQUENCY_INTERVAL * 60 * (i + VitalChartData.START_FILTER_INDEX), (float) signal[i]));
        }
        LineDataSet dataset = new LineDataSet(entryList, "");
        dataset.setDrawCircles(false);
        dataset.setColor(lineColor);
        LineData data = new LineData(dataset);
        chart.setData(data);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
