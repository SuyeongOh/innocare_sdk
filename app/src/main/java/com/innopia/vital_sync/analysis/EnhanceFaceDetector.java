package com.innopia.vital_sync.analysis;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.innopia.vital_sync.data.Config;

import java.util.List;

import androidx.annotation.NonNull;

public class EnhanceFaceDetector extends VisionProcessorBase<List<Face>> {
    private static final String FACE_DETECTION_MODEL_NAME = "face_detection_short_range.tflite";
    private static final float threshold = Config.THRESHOLD_DEFAULT;

    private DetectorListener mDetectorListener;

    private Context mContext;
    private Bitmap originalBitmap;

    private FaceDetector faceDetector;

    public EnhanceFaceDetector(Context context, DetectorListener listener){
        super(context);
        mContext = context;
        mDetectorListener = listener;
        setupFaceDetector();
    }

    public void setupFaceDetector(){
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .enableTracking()
                .build();

        faceDetector = FaceDetection.getClient(options);
    }

    public void detectAsync(InputImage image, Bitmap original){
        originalBitmap = original;
        Task<List<Face>> result = faceDetector.process(image);
        result.addOnCompleteListener(new OnCompleteListener<List<Face>>() {
            @Override
            public void onComplete(@NonNull Task<List<Face>> task) {
                mDetectorListener.onResults(
                        originalBitmap,
                        new ResultBundle(
                                result.getResult(),
                                image.getHeight(),
                                image.getWidth()
                        )
                );
            }
        });
    }

    @Override
    protected void onFailure(@NonNull Exception e) {

    }

    public void clearFaceDetector() {
        faceDetector.close();
        faceDetector = null;
    }

    @Override
    public void processBitmap(Bitmap bitmap) {
        super.processBitmap(bitmap);
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return faceDetector.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Face> results) {
        mDetectorListener.onResults(
                originalBitmap,
                new ResultBundle(
                        results,
                        originalBitmap.getHeight(),
                        originalBitmap.getWidth()
                )
        );
    }


    public static class ResultBundle{
        List<Face> results;
        public int inputImageHeight;
        public int inputImageWidth;

        public ResultBundle (
            List<Face> results,
            int inputImageHeight,
            int inputImageWidth
        ) {
            this.results = results;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }

        public List<Face> getResults(){
            return results;
        }
    }

    public interface DetectorListener {
        public void onError(String error, int errorCode);
        public void onResults(Bitmap original, ResultBundle resultBundle);
    }
}
