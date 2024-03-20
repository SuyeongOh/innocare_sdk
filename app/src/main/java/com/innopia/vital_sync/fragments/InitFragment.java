package com.innopia.vital_sync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.activities.MainActivity;
import com.innopia.vital_sync.R;
import com.innopia.vital_sync.data.Constant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InitFragment extends Fragment {

    private TextView guideTextView;
    private EditText bmiInputView;
    private EditText ageInputView;

    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale;
    private RadioButton radioButtonFemale;

    private EditText frameInputView;
    private EditText analysisTimeInputView;
    private EditText localIpInputView;
    private Button applyBtn;
    private Button recordBtn;
    private Switch serverResponseSwitch;
    private Switch cameraDirectionSwitch;
    private Switch largeFaceSwitch;
    private Switch smallFaceSwitch;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        guideTextView = view.findViewById(R.id.init_view_guide);

        bmiInputView = view.findViewById(R.id.init_view_bmi_input);
        ageInputView = view.findViewById(R.id.init_view_age_input);
        radioGroupGender = view.findViewById(R.id.init_view_gender_group);
        radioButtonMale = view.findViewById(R.id.init_view_gender_male);
        radioButtonFemale = view.findViewById(R.id.init_view_gender_female);

        radioGroupGender.check(R.id.init_view_gender_male);

        applyBtn = view.findViewById(R.id.init_btn_submit);
        recordBtn = view.findViewById(R.id.init_btn_record);
        cameraDirectionSwitch = view.findViewById(R.id.init_view_camera_switch);
        frameInputView = view.findViewById(R.id.init_view_frame_input);
        smallFaceSwitch = view.findViewById(R.id.init_view_small_face_switch);
        largeFaceSwitch = view.findViewById(R.id.init_view_large_face_switch);
        analysisTimeInputView = view.findViewById(R.id.init_view_analysis_time);
        localIpInputView = view.findViewById(R.id.init_view_ip_input);
        serverResponseSwitch = view.findViewById(R.id.init_view_server_switch);

        guideTextView.setText(
                String.format(getResources().getString(R.string.welcome_message), Config.USER_ID));

        recordBtn.setVisibility(View.GONE);
        if(Config.USER_ID.equals(getContext().getString(R.string.guest))){
            recordBtn.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        largeFaceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && smallFaceSwitch.isChecked()){
                    smallFaceSwitch.setChecked(false);
                }
            }
        });
        smallFaceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && largeFaceSwitch.isChecked()){
                    largeFaceSwitch.setChecked(false);
                }
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new RecordFragment());
            }
        });

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


                if(radioGroupGender.getCheckedRadioButtonId() == R.id.init_view_gender_female){
                    Config.USER_GENDER = "female";
                }else if(radioGroupGender.getCheckedRadioButtonId() == R.id.init_view_gender_male){
                    Config.USER_GENDER = "male";
                }
                Config.SERVER_RESPONSE_MODE = serverResponseSwitch.isChecked();
                //Config.LARGE_FACE_MODE = largeFaceSwitch.isChecked();
                Config.SMALL_FACE_MODE = smallFaceSwitch.isChecked();
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
