package com.innopia.vital_sync.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class AutoFitSurfaceView extends SurfaceView {

    public AutoFitSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private double aspectRatio;

    public void setAspectRatio(int x, int y) {
        aspectRatio = x / (float)y;
        requestLayout(); // View를 다시 레이아웃하는 것을 요청하여 크기를 조정합니다.
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (aspectRatio != 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);


            // 비율을 기반으로 View의 크기를 조정합니다.
            if (width > 0) {
                int newHeight = (int) (width / aspectRatio);
                setMeasuredDimension(width, newHeight);
            } else if (height > 0) {
                int newWidth = (int) (height * aspectRatio);
                setMeasuredDimension(newWidth, height);
            }
        }
    }
}
