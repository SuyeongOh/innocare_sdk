package com.vitalsync.vital_sync.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonHighlightAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;
import com.vitalsync.vital_sync.service.login.LoginClient;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.service.login.LoginRequest;
import com.vitalsync.vital_sync.service.login.LoginResponse;
import com.vitalsync.vital_sync.ui.CommonPopupView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoginFragment extends Fragment implements LoginClient.LoginResponseListener {

    private static final String USER_ID_KEY = "userID";
    private EditText userIdEditText;
    private Button loginButton;
    private ProgressBar loadingView;
    private CommonPopupView popupView;

    private SharedPreferences loginCookie;

    public LoginFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        userIdEditText = view.findViewById(R.id.editTextAccountId);
        loginButton = view.findViewById(R.id.view_login_button);
        loadingView = view.findViewById(R.id.view_login_loading);

        View viewNoDetectionPopup = inflater.inflate(R.layout.layout_detection_popup, container, false);
        TextView popupTextView = viewNoDetectionPopup.findViewById(R.id.text_face_popup);
        popupTextView.setText(R.string.login_fail);
        popupView = new CommonPopupView(requireContext(), viewNoDetectionPopup);

        viewNoDetectionPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.dismiss();
            }
        });

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginCookie = getContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String id = loginCookie.getString(USER_ID_KEY, "");
        if(!id.equals("")){
            userIdEditText.setHint("");
            userIdEditText.setText(id);
        }
    }

    private void login() {
        String userId = userIdEditText.getText().toString();
        //TODO DB서버로 연결 후 진행
        LoginClient.getInstance().login(new LoginRequest(userId, ""), this);
        loadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(LoginResponse response) {
        loadingView.setVisibility(View.GONE);
        String inputID = userIdEditText.getText().toString();
        if(!Config.USER_ID.equals(inputID)){
            saveID(inputID);
        }
        Config.USER_ID = inputID;
        MainActivity activity = (MainActivity) getActivity();
        activity.replaceFragment(new InitFragment());
    }

    @Override
    public void onError(String message) {
        //Alert Dialog
        loadingView.setVisibility(View.GONE);
        popupView.show();
    }

    public void saveID(String ID){
        SharedPreferences.Editor editor = loginCookie.edit();
        editor.putString(USER_ID_KEY, ID);
        editor.apply();
    }
}
