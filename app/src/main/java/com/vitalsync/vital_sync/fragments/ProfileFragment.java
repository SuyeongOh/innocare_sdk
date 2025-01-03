package com.vitalsync.vital_sync.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.ui.ProfileAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileFragment extends Fragment {
    private RecyclerView recyclerView;
    private Button btnSubmit;
    private Context mContext;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mContext = getContext();
        recyclerView = view.findViewById(R.id.profile_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        ProfileAdapter adapter = new ProfileAdapter(getContext(), getProfileInfoList());
        recyclerView.setAdapter(adapter);

        btnSubmit = view.findViewById(R.id.profile_submit);
        return view;
    }

    private ArrayList<String> getProfileInfoList(){
        if(mContext == null) return null;

        ArrayList<String> profileInfoList = new ArrayList<>();
        profileInfoList.add(mContext.getString(R.string.profile_weight));
        profileInfoList.add(mContext.getString(R.string.profile_height));
        profileInfoList.add(mContext.getString(R.string.profile_age));
        profileInfoList.add(mContext.getString(R.string.profile_SBP));
        profileInfoList.add(mContext.getString(R.string.profile_DBP));

        return profileInfoList;
    }
}
