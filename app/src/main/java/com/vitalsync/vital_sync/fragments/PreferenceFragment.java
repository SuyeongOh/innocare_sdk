package com.vitalsync.vital_sync.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.data.Constant;
import com.vitalsync.vital_sync.ui.CommonPopupView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PreferenceFragment extends Fragment {

    private final String PREFERENCE_KEY = "preference";
    private final String PREFERENCE_ANALYSIS_TIME_KEY = PREFERENCE_KEY + "_analysisTime";
    private final String PREFERENCE_DEVIP_KEY = PREFERENCE_KEY + "_devIP";
    private final String PREFERENCE_CAM_DIRECTION_KEY = PREFERENCE_KEY + "_camDirection";
    private final String PREFERENCE_TRACKING_KEY = PREFERENCE_KEY + "_faceTracking";

    private EditText analysisTimeView;
    private EditText devIpView;
    private Switch rearCameraView;
    private Switch trackingView;

    private CommonPopupView popupView;
    private Button btnSubmit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preference, container, false);

        analysisTimeView = view.findViewById(R.id.preference_analysis_time);
        devIpView = view.findViewById(R.id.preference_ip);
        rearCameraView = view.findViewById(R.id.preference_camera_direction);
        trackingView = view.findViewById(R.id.preference_face_tracking);

        View inputErrorPopup = inflater.inflate(R.layout.layout_detection_popup, container, false);
        TextView popupTextView = inputErrorPopup.findViewById(R.id.text_face_popup);
        popupTextView.setText(R.string.preference_input_error);
        popupView = new CommonPopupView(requireContext(), inputErrorPopup);

        inputErrorPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.dismiss();
            }
        });

        btnSubmit = view.findViewById(R.id.preference_submit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences pref = getContext().getSharedPreferences("PREFERENCE_KEY", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();

                    String analysisTime = analysisTimeView.getText().toString();
                    if(!analysisTime.equals("")){
                        Config.ANALYSIS_TIME = Integer.parseInt(analysisTime);
                        editor.putInt(PREFERENCE_ANALYSIS_TIME_KEY, Integer.parseInt(analysisTime));
                    }
                    String devIp = devIpView.getText().toString();
                    if(!devIp.equals("")){
                        Config.LOCAL_SERVER_ADDRESS = devIp;
                        editor.putString(PREFERENCE_DEVIP_KEY, devIp);
                    }
                    if(rearCameraView.isChecked()){
                        Config.USE_CAMERA_DIRECTION = Constant.CAMERA_DIRECTION_BACK;
                    }
                    if(trackingView.isChecked()){
                        Config.FLAG_TRACKING_FACE = true;
                    }
                    editor.putBoolean(PREFERENCE_CAM_DIRECTION_KEY, rearCameraView.isChecked());
                    editor.putBoolean(PREFERENCE_TRACKING_KEY, trackingView.isChecked());
                    editor.apply();

                } catch (Exception e){
                    popupView.show();
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences pref = getContext().getSharedPreferences("PREFERENCE_KEY", Context.MODE_PRIVATE);
        int analysisTime = pref.getInt(PREFERENCE_ANALYSIS_TIME_KEY, 20);
        String devIp = pref.getString(PREFERENCE_DEVIP_KEY, "192.0.0.1");
        boolean camDirection = pref.getBoolean(PREFERENCE_CAM_DIRECTION_KEY, false);
        boolean trackingFace = pref.getBoolean(PREFERENCE_TRACKING_KEY, false);

        analysisTimeView.setText(analysisTime);
        devIpView.setText(devIp);
        rearCameraView.setChecked(camDirection);
        trackingView.setChecked(trackingFace);
    }
}
