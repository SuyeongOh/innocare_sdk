package com.vitalsync.vital_sync.activities;

import android.Manifest;
import android.os.Bundle;

import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.fragments.LoginFragment;
import com.vitalsync.vital_sync.fragments.SplashFragment;
import com.vitalsync.vital_sync.video.VitalTestDataset;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {

    private Fragment currentFragment;
    private static final String[] LIST_NEW_SDK_PERMISSION = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_MEDIA_AUDIO,
            android.Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(LIST_NEW_SDK_PERMISSION, 1);

        //반드시 상용빌드시 VIDEO_TEST false
        if(Config.FLAG_VIDEO_TEST){
            VitalTestDataset test = new VitalTestDataset(this);
            test.runTest();
            //finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //최초시작 select target fragment
        if(currentFragment == null){
            SplashFragment fragment = new SplashFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainerView, fragment)
                    .commit();
        } else {
            LoginFragment fragment = new LoginFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainerView, fragment)
                    .commit();
            currentFragment = fragment;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSupportFragmentManager().beginTransaction().remove(currentFragment).commitAllowingStateLoss();
    }

    public void replaceFragment(Fragment fragment){
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.animation_fade_in, R.anim.animation_fade_out)
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
