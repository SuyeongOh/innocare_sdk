package com.inniopia.funnylabs_sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;
import com.inniopia.funnylabs_sdk.data.Constant;
import com.inniopia.funnylabs_sdk.utils.FileUtils;

import java.util.Collections;
import java.util.List;

import androidx.camera.core.ImageProxy;

public class EnhanceFaceDetector {
    private static final String FACE_DETECTION_MODEL_NAME = "face_detection_short_range.tflite";
    private static final float threshold = Config.THRESHOLD_DEFAULT;
    private static final int currentDelegate = Constant.DELEGATE_CPU;
    private static final RunningMode runningMode = RunningMode.LIVE_STREAM;

    private DetectorListener mDetectorListener;

    private Context mContext;
    private Bitmap originalBitmap;

    private FaceDetector faceDetector;

    private android.media.FaceDetector detector;

    public EnhanceFaceDetector(Context context, DetectorListener listener){
        mContext = context;
        mDetectorListener = listener;
        setupFaceDetector();
    }

    public void setupFaceDetector(){
        BaseOptions.Builder baseoptionBuilder = BaseOptions.builder();

        if(currentDelegate == Constant.DELEGATE_CPU){
            baseoptionBuilder.setDelegate(Delegate.CPU);
        }else if(currentDelegate == Constant.DELEGATE_GPU){
            baseoptionBuilder.setDelegate(Delegate.GPU);
        }
        baseoptionBuilder.setModelAssetPath(FACE_DETECTION_MODEL_NAME);

        if(mDetectorListener == null){
            throw new IllegalStateException("faceDetectorListener must be set");
        }

        try{
            FaceDetector.FaceDetectorOptions.Builder optionBuilder = FaceDetector.FaceDetectorOptions.builder()
                    .setBaseOptions(baseoptionBuilder.build())
                    .setMinDetectionConfidence(threshold)
                    .setRunningMode(runningMode);

            if(runningMode.equals(RunningMode.LIVE_STREAM)){
                optionBuilder.setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError);
            }else if(runningMode.equals(RunningMode.IMAGE)){
                optionBuilder.setResultListener(this::returnLivestreamResult)
                        .setErrorListener(this::returnLivestreamError);
            }

            FaceDetector.FaceDetectorOptions options = optionBuilder.build();
            faceDetector = FaceDetector.createFromOptions(mContext, options);
        } catch (Exception e){
            e.printStackTrace();
            mDetectorListener.onError(e.getMessage(), 1);
        }
    }

    public void detectVideoFile(){
        Vital vital = new Vital(mContext);
        Uri videoUri = Uri.EMPTY;
        try{
            videoUri = Uri.parse(FileUtils.assetFilePath(mContext, "ubfc_subject_1.mp4"));
        } catch (Exception e){
            e.printStackTrace();
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, videoUri);

        FaceImageModel faceImageModel = null;

        int width = retriever.getFrameAtIndex(0).getWidth();
        int height = retriever.getFrameAtIndex(0).getHeight();

        detector = new android.media.FaceDetector(width, height, 1);
        android.media.FaceDetector.Face[] face = new android.media.FaceDetector.Face[1];
        //detector.findFaces(ImageUtils.convertARGB8888ToRGB565(bitmapImage), face);

        int faceNum = 0;
        int frameIdx = 0;
        PointF midPoint = new PointF();

        while(faceNum == 0){
            Bitmap curFrame = retriever.getFrameAtIndex(frameIdx);
            originalBitmap = curFrame;
            Bitmap inputFrame = curFrame.copy(Bitmap.Config.RGB_565, false);
            faceNum = detector.findFaces(inputFrame, face);
            frameIdx++;
        }
        face[0].getMidPoint(midPoint);
        int left = (int)(midPoint.x - face[0].eyesDistance());
        int right = (int)(midPoint.x + face[0].eyesDistance());
        int top = (int)(midPoint.y - face[0].eyesDistance());
        int bottom = (int)(midPoint.y + face[0].eyesDistance());

        for(int i = 0; i < Config.ANALYSIS_TIME * Config.TARGET_FRAME; i++){
            Bitmap curFrame = retriever.getFrameAtIndex(frameIdx);
            originalBitmap = curFrame;

            Bitmap croppedFaceBitmap = Bitmap.createBitmap(curFrame, left, top, right-left, bottom-top);

            faceImageModel = new FaceImageModel(croppedFaceBitmap, i * 33);
            vital.calculatePOSVital(faceImageModel, true);
            frameIdx++;
        }
        long time = faceImageModel.frameUtcTimeMs;
    }

    public void detectLiveStreamFrame(ImageProxy imageProxy){
        long frametime = SystemClock.uptimeMillis();

        Bitmap buffer = Bitmap.createBitmap(imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888);
        buffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
        imageProxy.close();

        Matrix matrix = new Matrix();
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());
        if(Config.USE_CAMERA_DIRECTION == Constant.CAMERA_DIRECTION_FRONT){
            matrix.postScale(-1f, 1f, imageProxy.getWidth(), imageProxy.getHeight());
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(buffer, 0, 0, buffer.getWidth(), buffer.getHeight(), matrix, true);
        MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();
        originalBitmap = rotatedBitmap;
        detectAsync(mpImage, frametime);
    }

    public void detectAsync(MPImage mpImage, long frameTime){
        faceDetector.detectAsync(mpImage, frameTime);
    }

    public void detectAsync(MPImage mpImage, Bitmap original, long frameTime){
        originalBitmap = original;
        faceDetector.detectAsync(mpImage, frameTime);
    }

    private void returnLivestreamResult(FaceDetectorResult result, MPImage input){
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferencetime = finishTimeMs - result.timestampMs();

        mDetectorListener.onResults(
                input,
                originalBitmap,
                new ResultBundle(
                        Collections.singletonList(result),
                        inferencetime,
                        input.getHeight(),
                        input.getWidth()
                )
        );
    }

    private void returnLivestreamError(RuntimeException error) {
        mDetectorListener.onError(
                error.getMessage(), 1
        );
    }

    public boolean isClosed(){
        return faceDetector == null;
    }

    public void clearFaceDetector() {
        faceDetector.close();
        faceDetector = null;
    }


    public static class ResultBundle{
        List<FaceDetectorResult> results;
        long inferenceTime;
        int inputImageHeight;
        int inputImageWidth;

        public ResultBundle (
            List<FaceDetectorResult> results,
            long inferenceTime,
            int inputImageHeight,
            int inputImageWidth
        ) {
            this.results = results;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }

        public List<FaceDetectorResult> getResults(){
            return results;
        }
    }
    public interface DetectorListener {
        public void onError(String error, int errorCode);
        public void onResults(MPImage input, Bitmap original, ResultBundle resultBundle);
    }
}
