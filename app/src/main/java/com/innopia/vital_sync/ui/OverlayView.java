package com.innopia.vital_sync.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;
import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.R;

import androidx.annotation.Nullable;

public class OverlayView extends View {
    private FaceDetectorResult result;
    private Paint boxPaint = new Paint();
    private Float scaleFactorWidth = 1f;
    private Float scaleFactorHeight = 1f;
    private boolean isClear = false;
    private boolean isPortrait = false;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (result != null) {
            if (result.detections().size() > 0) {
                Detection person = result.detections().get(0);
                RectF boundingBox = person.boundingBox();

                float realTop = boundingBox.top * scaleFactorHeight;
                float realBottom = boundingBox.bottom * scaleFactorHeight;
                float realLeft = boundingBox.left * scaleFactorWidth;
                float realRight = boundingBox.right * scaleFactorWidth;

                @SuppressLint("DrawAllocation") //warning 방지 없어도 무관함
                RectF drawRect = new RectF(realLeft, realTop, realRight, realBottom);
                canvas.drawRect(drawRect, boxPaint);

                if (isClear) {
                    isClear = false;
                }
            }
            return;
        }
    }

    public void setResults(FaceDetectorResult detectResult, int imageWidth, int imageHeight, boolean portrait) {
        result = detectResult;
        scaleFactorWidth = getWidth() / (float) imageWidth;
        scaleFactorHeight = getHeight() / (float) imageHeight;
        isPortrait = portrait;
        invalidate();
    }

    public void clear() {
        //중복 방지 차원
        if (!isClear) {
            isClear = true;
            result = null;
            boxPaint.reset();
            invalidate();
            initPaints();
        }
    }

    private void initPaints() {
        boxPaint.setColor(getContext().getColor(R.color.mp_primary));
        boxPaint.setStrokeWidth(8);
        boxPaint.setStyle(Paint.Style.STROKE);
    }

    public boolean isSmallSize(RectF bBox) {
        return (bBox.width() < Config.FACE_MODEL_SIZE) || (bBox.height() < Config.FACE_MODEL_SIZE);
    }

    public boolean isOutBoundary(RectF bBox) {
        return (bBox.left + bBox.width() > Config.FULL_SIZE_OF_WIDTH)
                || bBox.top + bBox.height() > Config.FULL_SIZE_OF_HEIGHT;
    }
}
