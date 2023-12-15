package com.inniopia.funnylabs_sdk;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import static java.lang.Double.min;

public class OverlayView extends View {
    private FaceDetectorResult result;
    private Paint boxPaint = new Paint();
    private Float scaleFactor = 1f;

    private Rect bounds;

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(result != null) {
            if(result.detections().size() > 0){
                Detection person = result.detections().get(0);
                RectF boundingBox = person.boundingBox();

                List<NormalizedKeypoint> keypointList = person.keypoints().get();
                float left = getWidth() * (keypointList.get(0).x() + keypointList.get(4).x()) / 2;
                float right = getWidth() * (keypointList.get(1).x() + keypointList.get(5).x()) / 2;

                RectF drawRect = new RectF(left, boundingBox.top * scaleFactor
                        , right, boundingBox.bottom * scaleFactor);
                canvas.drawRect(drawRect, boxPaint);
            }
        }
    }

    public void setResults(FaceDetectorResult detectResult, int imageWidth, int imageHeight) {
        result = detectResult;
        scaleFactor = Math.min(getWidth() * 1f / imageWidth, getHeight()* 1f / imageHeight);
        invalidate();
    }

    private void clear(){
        result = null;
        boxPaint.reset();
        invalidate();
        initPaints();
    }

    private void initPaints(){
        boxPaint.setColor(getContext().getColor(R.color.mp_primary));
        boxPaint.setStrokeWidth(8);
        boxPaint.setStyle(Paint.Style.STROKE);
    }
}
