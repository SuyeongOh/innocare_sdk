package com.vitalsync.vital_sync.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonHighlightAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;
import com.vitalsync.vital_sync.data.Config;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GuestFragment extends Fragment {
    private Button btnGuestAnalysis;
    private Balloon mCheckupBallon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_main, container, false);

        btnGuestAnalysis = view.findViewById(R.id.view_login_guest_button);

        btnGuestAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config.USER_ID = getContext().getString(R.string.target_guest);
                loginGuest();
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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCheckupBallon.showAlignBottom(btnGuestAnalysis);
    }

    private void loginGuest() {
        MainActivity activity = (MainActivity) getActivity();
        activity.replaceFragment(new MainFragment());
    }
}
