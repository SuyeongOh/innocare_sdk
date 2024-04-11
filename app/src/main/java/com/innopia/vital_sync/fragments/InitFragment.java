package com.innopia.vital_sync.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.innopia.vital_sync.R;
import com.innopia.vital_sync.activities.MainActivity;
import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.data.Constant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InitFragment extends Fragment {

    private TextView guideTextView;
    private TextInputEditText bmiInputView;
    private TextInputEditText ageInputView;
    private TextInputEditText heightInputView;
    private TextInputEditText weightInputView;
    private TextInputEditText sbpInputView;
    private TextInputEditText dbpInputView;
    private RadioGroup radioGroupGender;

    private EditText frameInputView;
    private EditText analysisTimeInputView;
    private EditText localIpInputView;
    private Button applyBtn;
    private Button recordBtn;
    private Button clearGenderBtn;
    private Switch serverResponseSwitch;
    private Switch cameraDirectionSwitch;
    private Switch largeFaceSwitch;
    private Switch smallFaceSwitch;
    private SharedPreferences loginCookie;
    private final String USER_BMI_KEY = "bmi";
    private final String USER_WEIGHT_KEY = "weight";
    private final String USER_HEIGHT_KEY = "height";
    private final String USER_AGE_KEY = "age";
    private final String USER_GENDER_KEY = "gender";
    private final String USER_SBP_KEY = "sbp";
    private final String USER_DBP_KEY = "dbp";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init, container, false);
        guideTextView = view.findViewById(R.id.init_view_guide);

        bmiInputView = view.findViewById(R.id.init_view_bmi_input);
        ageInputView = view.findViewById(R.id.init_view_age_input);
        heightInputView = view.findViewById(R.id.init_view_height_input);
        weightInputView = view.findViewById(R.id.init_view_weight_input);
        sbpInputView = view.findViewById(R.id.init_view_sbp_input);
        dbpInputView = view.findViewById(R.id.init_view_dbp_input);

        radioGroupGender = view.findViewById(R.id.init_view_gender_group);
        radioGroupGender.setOnCheckedChangeListener(genderChangeListener);

        applyBtn = view.findViewById(R.id.init_btn_submit);
        recordBtn = view.findViewById(R.id.init_btn_record);
        clearGenderBtn = view.findViewById(R.id.init_btn_gender_clear);
        cameraDirectionSwitch = view.findViewById(R.id.init_view_camera_switch);
        frameInputView = view.findViewById(R.id.init_view_frame_input);
        smallFaceSwitch = view.findViewById(R.id.init_view_small_face_switch);
        largeFaceSwitch = view.findViewById(R.id.init_view_large_face_switch);
        analysisTimeInputView = view.findViewById(R.id.init_view_analysis_time);
        localIpInputView = view.findViewById(R.id.init_view_ip_input);
        serverResponseSwitch = view.findViewById(R.id.init_view_server_switch);

        String welcomeMsg = String.format(getResources().getString(R.string.welcome_message), Config.USER_ID);
        SpannableStringBuilder spannableWelcome = new SpannableStringBuilder(welcomeMsg);
        //첫번째줄
        String targetMsg = welcomeMsg.split("-")[1];
        int startIdx = welcomeMsg.indexOf(targetMsg);
        int endIdx = startIdx + targetMsg.length();
        spannableWelcome.setSpan(
                new ForegroundColorSpan(Color.BLUE)
                , startIdx
                , endIdx
                , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        targetMsg = welcomeMsg.split("-")[3];
        startIdx = welcomeMsg.indexOf(targetMsg);
        endIdx = startIdx + targetMsg.length();
        spannableWelcome.setSpan(
                new ForegroundColorSpan(Color.BLUE)
                , startIdx
                , endIdx
                , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        guideTextView.setText(spannableWelcome);

        if (Config.USER_ID.equals(getContext().getString(R.string.guest))) {
            recordBtn.setVisibility(View.GONE);
        }
        clearGenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioGroupGender.clearCheck();
                heightInputView.setText("");
                bmiInputView.setText("");
                weightInputView.setText("");
                sbpInputView.setText("");
                dbpInputView.setText("");
                ageInputView.setText("");
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginCookie = getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        //String to Double
        bmiInputView.setText(loginCookie.getString(USER_BMI_KEY, ""));
        ageInputView.setText(loginCookie.getString(USER_AGE_KEY, ""));
        weightInputView.setText(loginCookie.getString(USER_WEIGHT_KEY, ""));
        heightInputView.setText(loginCookie.getString(USER_HEIGHT_KEY, ""));
        sbpInputView.setText(loginCookie.getString(USER_SBP_KEY, ""));
        dbpInputView.setText(loginCookie.getString(USER_DBP_KEY, ""));

        String gender = loginCookie.getString(USER_GENDER_KEY, "");

        if (gender.equals("female")) {
            radioGroupGender.check(R.id.init_view_gender_female);
        } else if (gender.equals("male")) {
            radioGroupGender.check(R.id.init_view_gender_male);
        }
        largeFaceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && smallFaceSwitch.isChecked()) {
                    smallFaceSwitch.setChecked(false);
                }
            }
        });
        smallFaceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && largeFaceSwitch.isChecked()) {
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
                String age = ageInputView.getText().toString();
                String weight = weightInputView.getText().toString();
                String height = heightInputView.getText().toString();
                String sbp = sbpInputView.getText().toString();
                String dbp = dbpInputView.getText().toString();

                String frame = frameInputView.getText().toString();
                String time = analysisTimeInputView.getText().toString();

                if (radioGroupGender.getCheckedRadioButtonId() == R.id.init_view_gender_female) {
                    Config.USER_GENDER = "female";
                } else if (radioGroupGender.getCheckedRadioButtonId() == R.id.init_view_gender_male) {
                    Config.USER_GENDER = "male";
                }

                SharedPreferences.Editor editor = loginCookie.edit();
                try {
                    Config.USER_BMI = Double.parseDouble(bmi);
                    editor.putString(USER_BMI_KEY, bmi);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Config.USER_AGE = Integer.parseInt(age);
                    editor.putString(USER_AGE_KEY, age);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Config.USER_WEIGHT = Double.parseDouble(weight);
                    editor.putString(USER_WEIGHT_KEY, weight);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Config.USER_HEIGHT = Double.parseDouble(height);
                    editor.putString(USER_HEIGHT_KEY, height);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Config.USER_SBP = Double.parseDouble(sbp);
                    editor.putString(USER_SBP_KEY, sbp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Config.USER_DBP = Double.parseDouble(dbp);
                    editor.putString(USER_DBP_KEY, dbp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                editor.putString(USER_GENDER_KEY, Config.USER_GENDER);
                editor.apply();

                try {
                    Config.TARGET_FRAME = Integer.parseInt(frame);
                } catch (Exception e) {
                    Config.TARGET_FRAME = 30;
                }
                try {
                    Config.ANALYSIS_TIME = Integer.parseInt(time);
                } catch (Exception e) {
                    Config.ANALYSIS_TIME = 20;
                }

                if (!localIpInputView.getText().toString().equals("")) {
                    Config.LOCAL_SERVER_ADDRESS = localIpInputView.getText().toString();
                }


                Config.SERVER_RESPONSE_MODE = serverResponseSwitch.isChecked();
                //Config.LARGE_FACE_MODE = largeFaceSwitch.isChecked();
                Config.SMALL_FACE_MODE = smallFaceSwitch.isChecked();
                if (cameraDirectionSwitch.isChecked()) {
                    Config.USE_CAMERA_DIRECTION = Constant.CAMERA_DIRECTION_BACK;
                }

                bmiInputView.clearFocus();
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new MainFragment());
            }
        });
    }

    private RadioGroup.OnCheckedChangeListener genderChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            Log.d("vital", "check changed : " + checkedId);
            if (checkedId == group.findViewById(R.id.init_view_gender_male).getId()) {
                if (heightInputView.getText().toString().equals("")) heightInputView.setText("174");
                if (bmiInputView.getText().toString().equals("")) bmiInputView.setText("25");
                if (weightInputView.getText().toString().equals("")) weightInputView.setText("76");
                if (sbpInputView.getText().toString().equals("")) sbpInputView.setText("120");
                if (dbpInputView.getText().toString().equals("")) dbpInputView.setText("80");
                if (ageInputView.getText().toString().equals("")) ageInputView.setText("40");
            } else {
                if (heightInputView.getText().toString().equals("")) heightInputView.setText("158");
                if (bmiInputView.getText().toString().equals("")) bmiInputView.setText("22.5");
                if (weightInputView.getText().toString().equals("")) weightInputView.setText("56");
                if (sbpInputView.getText().toString().equals("")) sbpInputView.setText("120");
                if (dbpInputView.getText().toString().equals("")) dbpInputView.setText("80");
                if (ageInputView.getText().toString().equals("")) ageInputView.setText("40");
            }
        }
    };
}
