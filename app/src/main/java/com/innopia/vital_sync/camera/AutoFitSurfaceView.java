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

    private Bitmap mBitmap;
    private double aspectRatio = 0;

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        aspectRatio = getWidth()/(double)bitmap.getWidth();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mBitmap != null) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);

            float bitmapWidth = mBitmap.getWidth();
            float bitmapHeight = mBitmap.getHeight();

            // 이미지의 비율을 계산합니다.
            float aspectRatio = bitmapWidth / bitmapHeight;

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            // 비트맵을 화면에 그립니다.
            Bitmap resizeBitmap = Bitmap.createScaledBitmap(mBitmap, getWidth(), getHeight(), false);
            canvas.drawBitmap(resizeBitmap, 0, 0, null);
        }
    }
}
