package com.innopia.vital_sync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.innopia.vital_sync.R;
import com.innopia.vital_sync.activities.ResultActivity;
import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.ui.GtAdapter;

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
                for(int i = 0; i < adapter.getItemCount(); i++){
                    HashMap<String, String> inputData = adapter.getDataMap();

                    for(String label : Config.GT_LABEL_LIST){
                        String data = inputData.get(label);
                        //TODO GT : label+data+measureTime

                    }
                }
                ResultActivity activity = (ResultActivity) getActivity();
                activity.replaceFragment(new ResultFragment());
            }
        });
        return view;
    }
}
