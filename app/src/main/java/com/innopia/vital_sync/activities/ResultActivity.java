package com.innopia.vital_sync.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.innopia.vital_sync.R;
import com.innopia.vital_sync.data.ResultVitalSign;
import com.innopia.vital_sync.data.VitalChartData;
import com.innopia.vital_sync.fragments.GtinputFragment;
import com.innopia.vital_sync.fragments.LoginFragment;

import java.util.ArrayList;
import java.util.List;

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
