package com.vitalsync.vital_sync.fragments;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.polar.sdk.api.model.PolarEcgData;
import com.polar.sdk.api.model.PolarHrData;
import com.polar.sdk.api.model.PolarPpgData;
import com.polar.sdk.api.model.PolarPpiData;
import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;
import com.vitalsync.vital_sync.analysis.PolarAnalysisManager;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.data.Constant;

import java.util.ArrayList;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InitFragment extends Fragment{

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
    private EditText polarIdInputView;
    private EditText polarVerityIdInputView;
    private Button applyBtn;
    private Button recordBtn;
    private Button connectVerityBtn;
    private Button connectH10Btn;
    private Button clearGenderBtn;
    private ProgressBar polarProgressBar;
    private ProgressBar polarVerityProgressBar;
    private ImageView polarConnectResult;
    private ImageView polarVerityConnectResult;

    private SharedPreferences loginCookie;
    private final String USER_TABLE_NAME = "userInfo";
    private final String USER_BMI_KEY = "bmi";
    private final String USER_WEIGHT_KEY = "weight";
    private final String USER_HEIGHT_KEY = "height";
    private final String USER_AGE_KEY = "age";
    private final String USER_GENDER_KEY = "gender";
    private final String USER_SBP_KEY = "sbp";
    private final String USER_DBP_KEY = "dbp";
    private final String USER_POLAR_KEY = "polar";
    private final String USER_POLAR_VERITY_KEY = "verity";
    private String polarH10DeviceId;
    private String polarVerityDeviceId;

    private PolarAnalysisManager polarH10Manager;
    private PolarAnalysisManager polarVerityManager;



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
        connectH10Btn = view.findViewById(R.id.init_view_polar_connect);
        connectVerityBtn = view.findViewById(R.id.init_view_polar_verity_connect);
        clearGenderBtn = view.findViewById(R.id.init_btn_gender_clear);
        frameInputView = view.findViewById(R.id.init_view_frame_input);
        analysisTimeInputView = view.findViewById(R.id.init_view_analysis_time);
        localIpInputView = view.findViewById(R.id.init_view_ip_input);

        polarIdInputView = view.findViewById(R.id.init_view_polar_id);
        polarProgressBar = view.findViewById(R.id.init_view_polar_progress);
        polarConnectResult = view.findViewById(R.id.init_view_connect_result);

        polarVerityIdInputView = view.findViewById(R.id.init_view_polar_verity_id);
        polarVerityProgressBar = view.findViewById(R.id.init_view_polar_verity_progress);
        polarVerityConnectResult = view.findViewById(R.id.init_view_verity_connect_result);

        loginCookie = getContext().getSharedPreferences(USER_TABLE_NAME, Context.MODE_PRIVATE);

        if (Config.USER_ID.equals(getContext().getString(R.string.target_guest))) {
            analysisTimeInputView.setVisibility(View.GONE);
            localIpInputView.setVisibility(View.GONE);
        }
        String welcomeMsg = String.format(getResources().getString(R.string.welcome_message), Config.USER_ID);
        SpannableStringBuilder spannableWelcome = new SpannableStringBuilder(welcomeMsg);
        //첫번째줄
        String targetMsg = welcomeMsg.split("-")[1];
        int startIdx = welcomeMsg.indexOf(targetMsg);
        int endIdx = startIdx + targetMsg.length();
        spannableWelcome.setSpan(
                new ForegroundColorSpan(Color.BLUE), startIdx, endIdx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        targetMsg = welcomeMsg.split("-")[3];
        startIdx = welcomeMsg.indexOf(targetMsg);
        endIdx = startIdx + targetMsg.length();
        spannableWelcome.setSpan(
                new ForegroundColorSpan(Color.BLUE), startIdx, endIdx, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
        connectH10Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences.Editor editor = loginCookie.edit();
                    Config.USER_POLAR_ID = polarIdInputView.getText().toString();
                    polarH10DeviceId = polarIdInputView.getText().toString();
                    editor.putString(USER_POLAR_KEY, Config.USER_POLAR_ID);
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!checkBT()) {
                    Toast.makeText(getContext(), "Bluetooth status :: off", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("Polar", "try to connect device :: " + polarH10DeviceId);
                polarH10Manager = PolarAnalysisManager.getH10Instance();
                polarH10Manager.init(getActivity().getApplicationContext(), polarH10DeviceId);
                polarH10Manager.setDeviceStatusListener(statusListener);
                polarH10Manager.connect();
            }
        });

        connectVerityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SharedPreferences.Editor editor = loginCookie.edit();
                    Config.USER_POLAR_VERITY_ID = polarVerityIdInputView.getText().toString();
                    polarVerityDeviceId = polarVerityIdInputView.getText().toString();
                    editor.putString(USER_POLAR_VERITY_KEY, Config.USER_POLAR_VERITY_ID);
                    editor.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!checkBT()) {
                    Toast.makeText(getContext(), "Bluetooth status :: off", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("Polar", "try to connect device :: " + polarVerityDeviceId);
                polarVerityManager = PolarAnalysisManager.getVerityInstance();
                polarVerityManager.init(getActivity().getApplicationContext(), polarVerityDeviceId);
                polarVerityManager.setDeviceStatusListener(statusVerityListener);
                polarVerityManager.connect();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        polarH10DeviceId = loginCookie.getString(USER_POLAR_KEY, "");
        polarVerityDeviceId = loginCookie.getString(USER_POLAR_VERITY_KEY, "");
        //String to Double
        bmiInputView.setText(loginCookie.getString(USER_BMI_KEY, ""));
        ageInputView.setText(loginCookie.getString(USER_AGE_KEY, ""));
        weightInputView.setText(loginCookie.getString(USER_WEIGHT_KEY, ""));
        heightInputView.setText(loginCookie.getString(USER_HEIGHT_KEY, ""));
        sbpInputView.setText(loginCookie.getString(USER_SBP_KEY, ""));
        dbpInputView.setText(loginCookie.getString(USER_DBP_KEY, ""));
        polarIdInputView.setText(polarH10DeviceId);
        polarVerityIdInputView.setText(polarVerityDeviceId);
        String gender = loginCookie.getString(USER_GENDER_KEY, "");

        if (gender.equals("female")) {
            radioGroupGender.check(R.id.init_view_gender_female);
        } else if (gender.equals("male")) {
            radioGroupGender.check(R.id.init_view_gender_male);
        }

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
                    Config.ANALYSIS_TIME = 60;
                }

                if (!localIpInputView.getText().toString().equals("")) {
                    Config.LOCAL_SERVER_ADDRESS = localIpInputView.getText().toString();
                }

                Config.USE_CAMERA_DIRECTION = Constant.CAMERA_DIRECTION_FRONT;

                bmiInputView.clearFocus();
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new MainFragment());
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
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

    private boolean checkBT() {
        try {
            Context context = requireContext();
            BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter btAdapter = btManager.getAdapter();
            if (btAdapter == null) {
                Toast.makeText(context, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                bluetoothOnActivityResultLauncher.launch(enableBtIntent);
            }

            if (Build.VERSION.SDK_INT >= 31) {
                if(context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED){
                    return false;
                }
                if(context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            } else {
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private final ActivityResultLauncher<Intent> bluetoothOnActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() != Activity.RESULT_OK) {
                                Log.w("Polar", "Bluetooth off");
                            }
                        }
                    });

    private PolarAnalysisManager.DeviceStatusListener statusListener = new PolarAnalysisManager.DeviceStatusListener() {
        @Override
        public void onConnected() {
            //연결대기하는데 Polar기기 전원을 뺐다껴줘야함
            polarProgressBar.setVisibility(View.GONE);
            polarConnectResult.setVisibility(View.VISIBLE);
            polarConnectResult.setImageResource(R.drawable.ic_check);
            Log.d("PolarAnalysisManager", "onConnected() . . .");
        }

        @Override
        public void onDisconnect() {
            polarConnectResult.setImageResource(R.drawable.ic_error);
        }

        @Override
        public void onConnecting() {
            polarProgressBar.setVisibility(View.VISIBLE);
            connectH10Btn.setVisibility(View.GONE);
            Log.d("PolarAnalysisManager", "onConnecting() . . .");
        }

        @Override
        public void onError() {
            polarProgressBar.setVisibility(View.GONE);
            connectH10Btn.setVisibility(View.VISIBLE);
            Log.d("PolarAnalysisManager", "onError() . . .");
        }
    };

    private PolarAnalysisManager.DeviceStatusListener statusVerityListener = new PolarAnalysisManager.DeviceStatusListener() {
        @Override
        public void onConnected() {
            //연결대기하는데 Polar기기 전원을 뺐다껴줘야함
            polarVerityProgressBar.setVisibility(View.GONE);
            polarVerityConnectResult.setVisibility(View.VISIBLE);
            polarVerityConnectResult.setImageResource(R.drawable.ic_check);
            Log.d("PolarAnalysisManager", "onConnected() . . .");
        }

        @Override
        public void onDisconnect() {
            polarVerityConnectResult.setImageResource(R.drawable.ic_error);
        }

        @Override
        public void onConnecting() {
            polarVerityProgressBar.setVisibility(View.VISIBLE);
            connectVerityBtn.setVisibility(View.GONE);
            Log.d("PolarAnalysisManager", "onConnecting() . . .");
        }

        @Override
        public void onError() {
            polarVerityProgressBar.setVisibility(View.GONE);
            connectVerityBtn.setVisibility(View.VISIBLE);
            Log.d("PolarAnalysisManager", "onError() . . .");
        }
    };

}
