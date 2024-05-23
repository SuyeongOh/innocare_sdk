package com.vitalsync.vital_sync.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SplashFragment extends Fragment {
    private static final int SPLASH_DISPLAY_LENGTH = 2000; // 3 seconds

    private LinearLayout splashLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);

        splashLayout = view.findViewById(R.id.group_splash);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Load the animation
        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.animation_splash);
        splashLayout.startAnimation(anim);
        // Navigate to the next screen after the splash screen duration
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new GuideFragment());
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
