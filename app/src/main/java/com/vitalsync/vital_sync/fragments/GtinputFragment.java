package com.vitalsync.vital_sync.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.ResultActivity;
import com.vitalsync.vital_sync.service.gt.GtClient;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.data.ResultVitalSign;
import com.vitalsync.vital_sync.service.gt.GtRequest;
import com.vitalsync.vital_sync.service.gt.GtResponse;
import com.vitalsync.vital_sync.ui.GtAdapter;

import java.util.Arrays;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GtinputFragment extends Fragment {
    private RecyclerView gtRecyclerView;
    private Button gtInputButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gtinput, container, false);

        gtRecyclerView = view.findViewById(R.id.view_gt_recycler);

        gtRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        GtAdapter adapter = new GtAdapter(getContext(), Arrays.asList(Config.GT_LABEL_LIST));
        gtRecyclerView.setAdapter(adapter);

        gtInputButton = view.findViewById(R.id.view_gt_input_button);

        gtInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> inputData = adapter.getDataMap();

                //label 순서 ["HR", "RR", "HRV", "vital 1", "vital 2", "vital 3-1", "vital 3-2"]
                ResultVitalSign gtData = new ResultVitalSign();
                try{
                    gtData.HR = Double.parseDouble(inputData.get(Config.GT_LABEL_LIST[0]));
                } catch (Exception e){
                    gtData.HR = 0;
                    Log.e("Vital", "Ground Truth input Error : " + e.getMessage());
                }
                try{
                    gtData.HRV = Double.parseDouble(inputData.get(Config.GT_LABEL_LIST[1]));
                } catch (Exception e){
                    gtData.HRV = 0;
                    Log.e("Vital", "Ground Truth input Error : " + e.getMessage());
                }
                try{
                    gtData.RR = Double.parseDouble(inputData.get(Config.GT_LABEL_LIST[2]));
                } catch (Exception e){
                    gtData.RR = 0;
                    Log.e("Vital", "Ground Truth input Error : " + e.getMessage());
                }
                try{
                    gtData.SpO2 = Double.parseDouble(inputData.get(Config.GT_LABEL_LIST[3]));
                } catch (Exception e){
                    gtData.SpO2 = 0;
                    Log.e("Vital", "Ground Truth input Error : " + e.getMessage());
                }
                try{
                    gtData.STRESS = Double.parseDouble(inputData.get(Config.GT_LABEL_LIST[4]));
                } catch (Exception e){
                    gtData.STRESS = 0;
                    Log.e("Vital", "Ground Truth input Error : " + e.getMessage());
                }
                try{
                    gtData.SBP = Double.parseDouble(inputData.get(Config.GT_LABEL_LIST[5]));
                } catch (Exception e){
                    gtData.SBP = 0;
                    Log.e("Vital", "Ground Truth input Error : " + e.getMessage());
                }
                try{
                    gtData.DBP = Double.parseDouble(inputData.get(Config.GT_LABEL_LIST[6]));
                } catch (Exception e){
                    gtData.DBP = 0;
                    Log.e("Vital", "Ground Truth input Error : " + e.getMessage());
                }
                GtRequest request = new GtRequest(gtData, Config.Measure_Time, Config.USER_ID);
                GtClient.getInstance().postGT(request, gtListener);
                ResultActivity activity = (ResultActivity) getActivity();
                activity.replaceFragment(new ResultFragment());
            }
        });
        return view;
    }

    private final GtClient.GtResponseListener gtListener = new GtClient.GtResponseListener() {
        @Override
        public void onSuccess(GtResponse response) {
            Log.d("Vital", "Gt Input Success");
        }

        @Override
        public void onError(String message) {
            Log.d("Vital", "Gt Input Error : " + message);
        }
    };

}
