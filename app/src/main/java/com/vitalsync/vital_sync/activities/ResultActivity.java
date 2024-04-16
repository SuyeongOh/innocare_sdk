package com.vitalsync.vital_sync.activities;


import android.os.Bundle;

import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.data.VitalChartData;
import com.vitalsync.vital_sync.fragments.GtinputFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class ResultActivity extends AppCompatActivity {

    private Fragment currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        GtinputFragment fragment = new GtinputFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.ResultFragmentContainerView, fragment)
                .commit();

        currentFragment = fragment;
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSupportFragmentManager().beginTransaction().remove(currentFragment).commitAllowingStateLoss();
    }

    public void replaceFragment(Fragment fragment){
        currentFragment = fragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ResultFragmentContainerView, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
