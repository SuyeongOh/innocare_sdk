package com.inniopia.funnylabs_sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
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

public class OverlayView extends View {
    private FaceDetectorResult result;
    private Paint boxPaint = new Paint();
    private Float scaleFactor = 1f;

    private Rect bounds;

    private static final int FULL_SIZE_OF_DETECTION = 1280 * 720;
    //popup의 민감도를 바꾸려면 이부분을 바꾸세요.

    private float STANDARD_BIG_SIZE_OF_POPUP = 1f/2f;
    private float STANDARD_SMALL_SIZE_OF_POPUP = 1f/9f;

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

                List<NormalizedKeypoint> keypointList = person.keypoints().get();
//                float left = getWidth() * (keypointList.get(0).x() + keypointList.get(4).x()) / 2;
//                float right = getWidth() * (keypointList.get(1).x() + keypointList.get(5).x()) / 2;

                float realTop = boundingBox.top * scaleFactor;
                float realBottom = boundingBox.bottom * scaleFactor;
                float realLeft = boundingBox.left * scaleFactor;
                float realRight = boundingBox.right * scaleFactor;

                @SuppressLint("DrawAllocation") //warning 방지 없어도 무관함
                RectF drawRect = new RectF(realLeft, realTop, realRight, realBottom);
                canvas.drawRect(drawRect, boxPaint);
                if(isClear){
                    isClear = false;
                }
            }
        }
    }

    public void setResults(FaceDetectorResult detectResult, int imageWidth, int imageHeight) {
        result = detectResult;
        scaleFactor = Math.min(getWidth() * 1f / imageWidth, getHeight()* 1f / imageHeight);
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

    public boolean isBigSize(RectF bBox){
        //bBox는 현재 1280x720 기준으로 동작하기 때문에 해당 사이즈에 맞게 값을 설정해줘야함
        return !(bBox.width() * bBox.height() < FULL_SIZE_OF_DETECTION * STANDARD_BIG_SIZE_OF_POPUP);
    }

    public boolean isSmallSize(RectF bBox){
        return !(bBox.width() * bBox.height() > FULL_SIZE_OF_DETECTION * STANDARD_SMALL_SIZE_OF_POPUP);
    }
}
