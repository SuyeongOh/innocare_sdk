package com.innopia.vital_sync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.innopia.vital_sync.R;
import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.service.data.DataClient;
import com.innopia.vital_sync.service.data.DataResponse;
import com.innopia.vital_sync.service.data.DataService;
import com.innopia.vital_sync.ui.RecordAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RecordFragment extends Fragment implements Callback<List<DataResponse>> {
    private RecyclerView vitalLabelView;
    private ProgressBar chartLoadingView;
    private LineChart measuredDataChart;

    private final HashMap<String, List<Double>> dbData = new HashMap<>();
    private final List<String> dbTime = new ArrayList<>();
    public RecordFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        vitalLabelView = view.findViewById(R.id.record_label_recycler);
        RecordAdapter recordAdapter = new RecordAdapter(getContext(), Arrays.asList(Config.RECORD_LABEL_LIST));
        vitalLabelView.setAdapter(recordAdapter);

        chartLoadingView = view.findViewById(R.id.record_chart_loading);

        //BP / 나머지로 나눠서 chart 구현

        measuredDataChart = view.findViewById(R.id.record_chart_view);

        DataClient.getInstance().requestData(this);
        chartLoadingView.setVisibility(View.VISIBLE);


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
        //TODO chart data 지정'
        if(response.isSuccessful()){
            if(response.body() != null){
                initDBMap();
                for(DataResponse data : response.body()){
                    dbData.get("hr").add(data.hr);
                    dbData.get("hrv").add(data.hrv);
                    dbData.get("rr").add(data.rr);
                    dbData.get("spo2").add(data.spo2);
                    dbData.get("stress").add(data.stress);
                    dbData.get("sbp").add(data.sbp);
                    dbData.get("dbp").add(data.dbp);
                    dbTime.add(data.measurementTime);
                }
            }
        }
    }

    @Override
    public void onFailure(Call<List<DataResponse>> call, Throwable t) {
        chartLoadingView.setVisibility(View.GONE);
    }

    //TODO chart 가져오는 기능 만들기


}
