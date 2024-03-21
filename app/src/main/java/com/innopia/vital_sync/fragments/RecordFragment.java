package com.innopia.vital_sync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.innopia.vital_sync.R;
import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.ui.RecordAdapter;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecordFragment extends Fragment {
    private RecyclerView vitalLabelView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        vitalLabelView = view.findViewById(R.id.record_label_recycler);
        RecordAdapter recordAdapter = new RecordAdapter(getContext(), Arrays.asList(Config.RECORD_LABEL_LIST));
        vitalLabelView.setAdapter(recordAdapter);

        return view;
    }

    //TODO chart 가져오는 기능 만들기


}
