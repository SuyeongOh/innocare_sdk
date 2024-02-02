package com.inniopia.funnylabs_sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;

import java.util.List;

import androidx.annotation.Nullable;

public class OverlayView extends View {
    private FaceDetectorResult result;
    private Paint boxPaint = new Paint();
    private Float scaleFactorWidth = 1f;
    private Float scaleFactorHeight = 1f;
    private float radius;
    private Rect bounds;
    private static final int FULL_SIZE_OF_DETECTION = Config.FULL_SIZE_OF_WIDTH * Config.FULL_SIZE_OF_HEIGHT;
    //popup의 민감도를 바꾸려면 이부분을 바꾸세요.

    private float STANDARD_BIG_SIZE_OF_POPUP = 1f/2f;

    private boolean isClear = false;
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

                float realTop = boundingBox.top * scaleFactorHeight;
                float realBottom = boundingBox.bottom * scaleFactorHeight;
                float realLeft = boundingBox.left * scaleFactorWidth;
                float realRight = boundingBox.right * scaleFactorWidth;

                @SuppressLint("DrawAllocation") //warning 방지 없어도 무관함
                RectF drawRect = new RectF(realLeft, realTop, realRight, realBottom);
                canvas.drawRect(drawRect, boxPaint);

                if(isClear){
                    isClear = false;
                }
            }
            return;
        }
    }

    public void setResults(FaceDetectorResult detectResult, int imageWidth, int imageHeight) {
        result = detectResult;
        scaleFactorWidth = getWidth()/ (float)imageWidth;
        scaleFactorHeight = getHeight() / (float)imageHeight;
        invalidate();
    }

    public void clear(){
        //중복 방지 차원
        if(!isClear){
            isClear = true;
            result = null;
            boxPaint.reset();
            invalidate();
            initPaints();
        }
    }

    private void initPaints(){
        boxPaint.setColor(getContext().getColor(R.color.mp_primary));
        boxPaint.setStrokeWidth(8);
        boxPaint.setStyle(Paint.Style.STROKE);
    }

    public boolean isSmallSize(RectF bBox){
        return (bBox.width() < Config.FACE_MODEL_SIZE) || (bBox.height() < Config.FACE_MODEL_SIZE);
    }

    public boolean isOutBoundary(RectF bBox){
        return (bBox.left + bBox.width() > Config.FULL_SIZE_OF_WIDTH)
                || bBox.top + bBox.height() > Config.FULL_SIZE_OF_HEIGHT;
    }
}
