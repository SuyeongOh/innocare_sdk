package com.innopia.vital_sync;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.innopia.vital_sync.data.Constant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InitFragment extends Fragment {

    private EditText bmiInputView;
    private EditText frameInputView;
    private EditText analysisTimeInputView;
    private EditText localIpInputView;
    private Button applyBtn;
    private Switch serverResponseSwitch;
    private Switch cameraDirectionSwitch;
    private Switch largeFaceSwitch;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);

        bmiInputView = view.findViewById(R.id.init_view_bmi_input);
        applyBtn = view.findViewById(R.id.init_btn_submit);
        cameraDirectionSwitch = view.findViewById(R.id.init_view_camera_switch);
        frameInputView = view.findViewById(R.id.init_view_frame_input);
        largeFaceSwitch = view.findViewById(R.id.init_view_large_face_switch);
        analysisTimeInputView = view.findViewById(R.id.init_view_analysis_time);
        localIpInputView = view.findViewById(R.id.init_view_ip_input);
        serverResponseSwitch = view.findViewById(R.id.init_view_server_switch);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String bmi = bmiInputView.getText().toString();
                String frame = frameInputView.getText().toString();
                String time = analysisTimeInputView.getText().toString();

                try{
                    Config.USER_BMI = Double.parseDouble(bmi);
                } catch (Exception e){
                    Config.USER_BMI = 20.1f;
                }
                try{
                    Config.TARGET_FRAME = Integer.parseInt(frame);
                } catch (Exception e){
                    Config.TARGET_FRAME = 30;
                }
                try{
                    Config.ANALYSIS_TIME = Integer.parseInt(time);
                } catch (Exception e){
                    Config.ANALYSIS_TIME = 20;
                }

                if(!localIpInputView.getText().toString().equals("")){
                    Config.LOCAL_SERVER_ADDRESS = localIpInputView.getText().toString();
                }

                Config.SERVER_RESPONSE_MODE = serverResponseSwitch.isChecked();
                Config.LARGE_FACE_MODE = largeFaceSwitch.isChecked();
                if(cameraDirectionSwitch.isChecked()){
                    Config.USE_CAMERA_DIRECTION = Constant.CAMERA_DIRECTION_BACK;
                }

                bmiInputView.clearFocus();
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new MainFragment());
            }
        });
    }
}
