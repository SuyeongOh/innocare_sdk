package com.vitalsync.vital_sync.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;
import com.vitalsync.vital_sync.data.ResultVitalSign;
import com.vitalsync.vital_sync.data.VitalChartData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ResultFragment extends Fragment {
    private LineChart greenChart;
    private LineChart smoothChart;
    private LineChart coreChart;
    private LineChart detrendChart;
    private LineChart bpfChart;
    private LineChart fftChart;
    private CombinedChart bvpChart;
    private LineChart hrChart;

    private Button restartBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        restartBtn = view.findViewById(R.id.result_recheck_btn);
        restartBtn.setText(String.format("재검사, frame : %d", VitalChartData.FRAME_RATE));
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VitalChartData.START_FILTER_INDEX = 0;
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });
        bindChart(view);
        setValue();

        return view;
    }

    private void bindChart(View rootView){
        greenChart = rootView.findViewById(R.id.g_chart);
        smoothChart = rootView.findViewById(R.id.smooth_chart);
        coreChart = rootView.findViewById(R.id.core_chart);
        detrendChart = rootView.findViewById(R.id.detrend_chart);
        bpfChart = rootView.findViewById(R.id.bpf_chart);
        bvpChart = rootView.findViewById(R.id.bvp_chart);
        fftChart = rootView.findViewById(R.id.fft_chart);
        hrChart = rootView.findViewById(R.id.hr_chart);


        initChart(greenChart, "G Signal");
        initChart(smoothChart, "Smooth");
        initChart(coreChart, "Core");
        initChart(detrendChart, "Detrend");
        initChart(bpfChart, "BPF");
        initChart(fftChart, "FFT");
        initChart(hrChart, "FFT-HR");
        initCombinedChart(bvpChart, "BVP");

        setLineChart();
    }

    @SuppressLint("SetTextI18n")
    private void setValue(){
        ResultVitalSign vitalsign = ResultVitalSign.vitalSignData;
    }

    private void setLineChart(){
        drawChart(greenChart, VitalChartData.R_SIGNAL, Color.RED, "");
        drawChart(greenChart, VitalChartData.G_SIGNAL, Color.GREEN, "");
        drawChart(greenChart, VitalChartData.B_SIGNAL, Color.BLUE, "");
        drawChart(smoothChart, VitalChartData.SMOOTH_R_SIGNAL, Color.RED, "");
        drawChart(smoothChart, VitalChartData.SMOOTH_G_SIGNAL, Color.GREEN, "");
        drawChart(smoothChart, VitalChartData.SMOOTH_B_SIGNAL, Color.BLUE, "");
        drawChart(coreChart, VitalChartData.CORE_SIGNAL, Color.MAGENTA, "");
        drawChart(detrendChart, VitalChartData.DETREND_SIGNAL, Color.CYAN, "");
        drawChart(bpfChart, VitalChartData.BPF_SIGNAL, Color.BLUE, "");
        drawCombinedChart(bvpChart, VitalChartData.BVP_SIGNAL, VitalChartData.HRV_PEAK, Color.MAGENTA);
        drawChart(fftChart, VitalChartData.FFT_SIGNAL, Color.CYAN, "");
        drawChart(hrChart, VitalChartData.HR_SIGNAL, Color.BLUE, "hr");
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

    private void initCombinedChart(CombinedChart chart, String description){
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

    private void drawChart(LineChart chart, double[] signal, int lineColor, String type){
        List<Entry> entryList = new ArrayList<>();
        if(type.equals("hr")){
            for (int i = 0; i < signal.length; i++) {
                entryList.add(
                        new Entry(VitalChartData.FREQUENCY_INTERVAL * 60 * (i + VitalChartData.START_FILTER_INDEX), (float) signal[i]));
            }
        } else {
            for (int i = 0; i < signal.length; i++) {
                entryList.add(new Entry(i, (float) signal[i]));
            }
        }

        LineDataSet dataset = new LineDataSet(entryList, "");
        dataset.setDrawCircles(false);
        dataset.setColor(lineColor);
        LineData data = new LineData(dataset);
        chart.setData(data);
        chart.setExtraTopOffset(5f);
        chart.setExtraBottomOffset(5f);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void drawCombinedChart(CombinedChart chart, double[] signal, int[] points, int lineColor){
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < signal.length; i++) {
            entryList.add(new Entry(i, (float) signal[i]));
        }
        LineDataSet dataset = new LineDataSet(entryList, "");
        dataset.setDrawCircles(false);
        dataset.setColor(lineColor);
        LineData data = new LineData(dataset);

        List<Entry> entryListPoint = new ArrayList<>();
        for (int point : points) {
            entryListPoint.add(new Entry(point, (float) signal[point]));
        }
        ScatterDataSet datasetPoint = new ScatterDataSet(entryListPoint, "");
        datasetPoint.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        datasetPoint.setColor(Color.RED);
        datasetPoint.setDrawValues(true);
        ScatterData dataPoint = new ScatterData(datasetPoint);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(data);
        combinedData.setData(dataPoint);

        chart.setData(combinedData);
        chart.setExtraTopOffset(5f);
        chart.setExtraBottomOffset(5f);
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
}
