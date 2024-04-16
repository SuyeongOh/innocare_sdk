package com.vitalsync.vital_sync.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vitalsync.vital_sync.R;

public class CustomCountdownView extends RelativeLayout {
    private TextView countdownTextView;

    public CustomCountdownView(Context context) {
        super(context);
        init(context);
    }

    public CustomCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomCountdownView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_countdown, this);

        countdownTextView = findViewById(R.id.countdownTextView);
    }

    public void reset(){
        countdownTextView.setText("");
    }
    public void setCountDownText(String seconds){
        countdownTextView.setText(seconds);
    }
}
