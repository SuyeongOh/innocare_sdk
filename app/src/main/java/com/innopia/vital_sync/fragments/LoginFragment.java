package com.innopia.vital_sync.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.innopia.vital_sync.R;
import com.innopia.vital_sync.activities.MainActivity;
import com.innopia.vital_sync.client.LoginClient;
import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.service.LoginRequest;
import com.innopia.vital_sync.service.LoginResponse;

public class LoginFragment extends Fragment implements LoginClient.LoginResponseListener {

    private EditText userIdEditText;
    private Button loginButton;
    private Button guestButton;
    private ProgressBar loadingView;
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
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new InitFragment());
            }
        });
        return view;
    }

    private void login() {
        String userId = userIdEditText.getText().toString();
        //TODO DB서버로 연결 후 진행
        LoginClient.getInstance().login(new LoginRequest(userId, ""), this);
        //TODO Loading View
        loadingView.setVisibility(View.VISIBLE);

    }

    private void loginGuest() {
        //
    }

    @Override
    public void onSuccess(LoginResponse response) {
        //Loading View Stop
        loadingView.setVisibility(View.GONE);

        Config.USER_ID = userIdEditText.getText().toString();

    }

    @Override
    public void onError(String message) {
        //Alert Dialog
        loadingView.setVisibility(View.GONE);

    }
}
