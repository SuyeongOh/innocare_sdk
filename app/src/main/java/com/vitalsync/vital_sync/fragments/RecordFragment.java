package com.vitalsync.vital_sync.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.service.data.DataClient;
import com.vitalsync.vital_sync.service.data.DataResponse;
import com.vitalsync.vital_sync.ui.RecordAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordFragment extends Fragment
        implements Callback<List<DataResponse>>, RecordAdapter.RecordDataLoadListener {
    private RecyclerView vitalLabelView;
    private ProgressBar chartLoadingView;
    private LineChart measuredDataChart;
    private ImageView prevPager;
    private ImageView nextPager;
    private Button dataSyncButton;
    private final HashMap<String, List<Double>> dbData = new HashMap<>();
    private final List<String> dbTime = new ArrayList<>();
    private final Callback<List<DataResponse>> dataCallback = this;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        prevPager = view.findViewById(R.id.record_label_pager_prev);
        nextPager = view.findViewById(R.id.record_label_pager_next);

        vitalLabelView = view.findViewById(R.id.record_label_recycler);


        RecordAdapter recordAdapter = new RecordAdapter(getContext(), Arrays.asList(Config.RECORD_LABEL_LIST));
        recordAdapter.setLoadListener(this);
        vitalLabelView.setAdapter(recordAdapter);

        vitalLabelView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                Log.d("vital", "last position : " + lastVisibleItemPosition);
                if (firstVisibleItemPosition != 0) {
                    prevPager.setVisibility(View.VISIBLE);
                } else {
                    prevPager.setVisibility(View.INVISIBLE);
                }

                if(lastVisibleItemPosition != vitalLabelView.getAdapter().getItemCount() - 1){
                    nextPager.setVisibility(View.VISIBLE);
                } else{
                    nextPager.setVisibility(View.INVISIBLE);
                }
            }
        });

        chartLoadingView = view.findViewById(R.id.record_chart_loading);

        //BP / 나머지로 나눠서 chart 구현

        measuredDataChart = view.findViewById(R.id.record_chart_view);

        DataClient.getInstance().requestData(this);
        chartLoadingView.setVisibility(View.VISIBLE);

        dataSyncButton = view.findViewById(R.id.record_syncdata_view);

        dataSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataClient.getInstance().requestData(dataCallback);
                chartLoadingView.setVisibility(View.VISIBLE);
                measuredDataChart.clear();
                recordAdapter.clearAllSelect();
            }
        });
        return view;
    }

    public void initDBMap(){
        dbData.put("hr", new ArrayList<>());
        dbData.put("hrv", new ArrayList<>());
        dbData.put("rr", new ArrayList<>());
        dbData.put("spo2", new ArrayList<>());
        dbData.put("stress", new ArrayList<>());
        dbData.put("sbp", new ArrayList<>());
        dbData.put("dbp", new ArrayList<>());
    }

    @Override
    public void onResponse(Call<List<DataResponse>> call, Response<List<DataResponse>> response) {
        chartLoadingView.setVisibility(View.GONE);
        if(response.isSuccessful()) {
            if (response.body() != null) {
                initDBMap();
                for (DataResponse data : response.body()) {
                    dbData.get("hr").add(data.hr);
                    dbData.get("hrv").add(data.hrv);
                    dbData.get("rr").add(data.rr);
                    dbData.get("spo2").add(data.spo2);
                    dbData.get("stress").add(data.stress);
                    dbData.get("sbp").add(data.sbp);
                    dbData.get("dbp").add(data.dbp);
                    dbTime.add(data.MeasurementTime);
                }
                initChart("");
            }
        }
    }

    @Override
    public void onFailure(Call<List<DataResponse>> call, Throwable t) {
        chartLoadingView.setVisibility(View.GONE);
    }

    private void initChart(String description){
        measuredDataChart.getDescription().setText(description);
        measuredDataChart.getDescription().setEnabled(true);
        Legend legend = measuredDataChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10);
        legend.setTextSize(13);
        legend.setTextColor(Color.parseColor("#A3A3A3"));
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        legend.setYEntrySpace(3);

        XAxis xAxis = measuredDataChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 데이터 표시 위치
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setSpaceMin(0.1f); // Chart 맨 왼쪽 간격 띄우기
        xAxis.setSpaceMax(0.1f); // Chart 맨 오른쪽 간격 띄우기

        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxisLeft = measuredDataChart.getAxisLeft();
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setTextColor(Color.BLACK);
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.resetAxisMinimum();
        yAxisLeft.setAxisLineWidth(2);

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxis = measuredDataChart.getAxisRight();
        yAxis.setDrawLabels(false); // label 삭제
        yAxis.setTextColor(Color.BLACK);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisLineWidth(2);
        yAxisLeft.resetAxisMinimum();
        yAxis.setAxisMaximum((float) 1); // 최댓값
        yAxis.setGranularity((float) 0.1);
    }

    private void changeChartLabel(String label){
        measuredDataChart.getDescription().setText(label);
    }

    private void drawData(List<Double> data){
        Log.d("vital", "onLoad Record ");
        List<Entry> entryList = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entryList.add(new Entry(i, data.get(i).floatValue()));
        }
        LineDataSet dataset = new LineDataSet(entryList, "");
        dataset.setDrawCircles(false);
        dataset.setLineWidth(5f);
        dataset.setColor(Color.CYAN);
        LineData lineData = new LineData(dataset);
        measuredDataChart.setData(lineData);
        //measuredDataChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dbTime));
        measuredDataChart.notifyDataSetChanged();
        measuredDataChart.setExtraBottomOffset(5f);
        measuredDataChart.invalidate();
    }

    @Override
    public void onLoad(Button btn) {
        switch (btn.getText().toString()){
            default:
                changeChartLabel(btn.getText().toString());
            case "HR":
                drawData(dbData.get("hr"));
                break;
            case "HRV":
                drawData(dbData.get("hrv"));
                break;
            case "RR":
                drawData(dbData.get("rr"));
                break;
            case "vital 1":
                drawData(dbData.get("spo2"));
                break;
            case "vital 2":
                drawData(dbData.get("stress"));
                break;

            case "vital 3":
                //vital 3 - bp
                drawData(dbData.get("sbp"));
                break;

        }
    }
}
