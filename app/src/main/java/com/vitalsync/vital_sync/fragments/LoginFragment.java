package com.vitalsync.vital_sync.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skydoves.balloon.ArrowOrientation;
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
import com.vitalsync.vital_sync.service.login.LoginService;
import com.vitalsync.vital_sync.service.login.UserInfo;
import com.vitalsync.vital_sync.ui.CommonPopupView;
import com.vitalsync.vital_sync.ui.UserListAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment implements LoginClient.LoginResponseListener {

    private static final String USER_ID_KEY = "userID";
    private EditText userIdEditText;
    private Button loginButton;
    private ProgressBar loadingView;
    private CommonPopupView popupView;
    private RecyclerView userListView;

    private SharedPreferences loginCookie;
    private Balloon mCheckupBallon;

    private final List<UserInfo> userInfoList = new ArrayList<>();
    private UserListAdapter userListAdapter;

    String userId = "";
    public LoginFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        userIdEditText = view.findViewById(R.id.view_login_id);
        loginButton = view.findViewById(R.id.view_login_button);
        loadingView = view.findViewById(R.id.view_login_loading);
        userListView = view.findViewById(R.id.recyclerUserView);

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

        mCheckupBallon = new Balloon.Builder(getContext())
                .setHeight(BalloonSizeSpec.WRAP)
                .setWidth(BalloonSizeSpec.WRAP)
                .setText(getString(R.string.rule_measure))
                .setTextColorResource(R.color.color_btn_text_default_1)
                .setTextSize(15f)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowSize(10)
                .setArrowPosition(0.5f)
                .setPadding(12)
                .setCornerRadius(8f)
                .setBackgroundColorResource(R.color.lightBlueGrey)
                .setBalloonAnimation(BalloonAnimation.FADE)
                .setBalloonHighlightAnimation(BalloonHighlightAnimation.HEARTBEAT)
                .setLifecycleOwner(this)
                .setFocusable(false)
                .build();


        LoginClient.getInstance().getUserList().enqueue(new Callback<List<UserInfo>>() {
            @Override
            public void onResponse(Call<List<UserInfo>> call, Response<List<UserInfo>> response) {
                for(UserInfo user : response.body()){
                    if(user.getUserId().contains("ku")){
                        userInfoList.add(user);
                    }
                }
                userListAdapter = new UserListAdapter(userInfoList, new UserListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(UserInfo user) {
                        login(user.getUserId());
                    }
                });
                userListView.setLayoutManager(new LinearLayoutManager(getContext()));
                userListView.setAdapter(userListAdapter);
                mCheckupBallon.showAlignTop(userListView);
            }

            @Override
            public void onFailure(Call<List<UserInfo>> call, Throwable t) {
                t.printStackTrace();
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
        userId = userIdEditText.getText().toString();
        //TODO DB서버로 연결 후 진행
        LoginClient.getInstance().login(new LoginRequest(userId, ""), this);
        loadingView.setVisibility(View.VISIBLE);
    }

    private void login(String user_id) {
        //TODO DB서버로 연결 후 진행
        user_id = user_id;
        LoginClient.getInstance().login(new LoginRequest(user_id, ""), this);
        loadingView.setVisibility(View.VISIBLE);
    }

    private void loginGuest() {
        MainActivity activity = (MainActivity) getActivity();
        activity.replaceFragment(new MainFragment()
        );
    }

    @Override
    public void onSuccess(LoginResponse response, String userId) {
        loadingView.setVisibility(View.GONE);
        if(!Config.USER_ID.equals(userId)){
            saveID(userId);
        }
        Config.USER_ID = userId;
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
