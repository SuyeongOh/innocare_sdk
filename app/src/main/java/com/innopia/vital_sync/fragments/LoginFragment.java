package com.innopia.vital_sync.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.innopia.vital_sync.R;
import com.innopia.vital_sync.activities.MainActivity;
import com.innopia.vital_sync.client.LoginClient;
import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.service.LoginRequest;
import com.innopia.vital_sync.service.LoginResponse;
import com.innopia.vital_sync.ui.CommonPopupView;

public class LoginFragment extends Fragment implements LoginClient.LoginResponseListener {

    private EditText userIdEditText;
    private Button loginButton;
    private Button guestButton;
    private ProgressBar loadingView;
    private CommonPopupView popupView;
    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        userIdEditText = view.findViewById(R.id.view_login_id);
        loginButton = view.findViewById(R.id.view_login_button);
        guestButton = view.findViewById(R.id.view_login_guest_button);
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

        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginGuest();
            }
        });
        return view;
    }

    private void login() {
        String userId = userIdEditText.getText().toString();
        //TODO DB서버로 연결 후 진행
        LoginClient.getInstance().login(new LoginRequest(userId, ""), this);
        loadingView.setVisibility(View.VISIBLE);
    }

    private void loginGuest() {
        MainActivity activity = (MainActivity) getActivity();
        activity.replaceFragment(new InitFragment());
    }

    @Override
    public void onSuccess(LoginResponse response) {
        //Loading View Stop
        loadingView.setVisibility(View.GONE);
        Config.USER_ID = userIdEditText.getText().toString();
        loginGuest();
    }

    @Override
    public void onError(String message) {
        //Alert Dialog
        loadingView.setVisibility(View.GONE);
        popupView.show();
    }
}
