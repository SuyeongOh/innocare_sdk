package com.vitalsync.vital_sync.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MemberFragment extends Fragment {
    private final String MANUAL_URL = "https://www.youtube.com/shorts/NGKVF5qSxgM";
    private View checkupButton;
    private View recordButton;
    private View profileButton;
    private View manualButton;
    private View preferenceButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member, container, false);
        checkupButton = view.findViewById(R.id.member_checkup);
        recordButton = view.findViewById(R.id.member_record);
        profileButton = view.findViewById(R.id.member_profile);
        manualButton = view.findViewById(R.id.member_manual);
        preferenceButton = view.findViewById(R.id.member_preference);

        checkupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.replaceFragment(new MainFragment());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.replaceFragment(new RecordFragment());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.replaceFragment(new ProfileFragment());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MANUAL_URL));
                startActivity(intent);
            }
        });
        preferenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.replaceFragment(new PreferenceFragment());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}
