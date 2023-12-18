package com.inniopia.funnylabs_sdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.MemoryFormat;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class Vital {
    private Module mPOSModule = null;
    private static final String POS_TORCH_MODULE_NAME = "POS.ptl";
    private static final int VIDEO_FRAME_RATE = 30;
    private final Result lastResult = new Result();

    private static final int FACE_WIDTH = 50;
    private static final int FACE_HEIGHT = 50;

    int l = (int)(VIDEO_FRAME_RATE * 1.6);

    static final int FACE_PIXEL_COUNT = FACE_WIDTH * FACE_HEIGHT;
    static final int FRAME_PIXEL_COUNT = FACE_PIXEL_COUNT * 3;
    private static final int BATCH_SIZE = 2;
    private final int[] face_pixels = new int[FACE_PIXEL_COUNT];

    private final List<Long> mUtcTimeTempList = new ArrayList<>();
    private final float[] inputFloatArray = new float[BATCH_SIZE * 3 * FRAME_WINDOW_SIZE  * FACE_WIDTH * FACE_HEIGHT];
    private static final int FRAME_WINDOW_SIZE = 300;

    public Vital(Context context) {
        try {
            mPOSModule = LiteModuleLoader.load(FileUtils.assetFilePath(context, POS_TORCH_MODULE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Result calculatePOSVital(@NonNull FaceImageModel faceImageModel){
        Bitmap faceBitmap =
                Bitmap.createScaledBitmap(faceImageModel.bitmap, FACE_WIDTH, FACE_HEIGHT, false);
        faceBitmap.getPixels(face_pixels, 0, FACE_WIDTH, 0, 0, FACE_WIDTH, FACE_HEIGHT);
        int pixelOffset = FRAME_PIXEL_COUNT * mUtcTimeTempList.size();
        for (int i = 0; i < FACE_PIXEL_COUNT; i++) {
            final int c = face_pixels[i];
            float r = ((c >> 16) & 0xff) / 255.0f;
            float g = ((c >> 8) & 0xff) / 255.0f;
            float b = ((c) & 0xff) / 255.0f;

            try{
                inputFloatArray[pixelOffset + 3 * i] = r;
                inputFloatArray[pixelOffset + 3 * i + 1] = g;
                inputFloatArray[pixelOffset + 3 * i + 2] = b;
            } catch (Exception e){
                return lastResult;
            }
        }

        mUtcTimeTempList.add(faceImageModel.frameUtcTimeMs);

        if (mUtcTimeTempList.size() == FRAME_WINDOW_SIZE * BATCH_SIZE) {
            FloatBuffer buffer = Tensor.allocateFloatBuffer(BATCH_SIZE * 3 * FRAME_WINDOW_SIZE  * FACE_HEIGHT * FACE_WIDTH);
            buffer.put(inputFloatArray);
            Tensor inputTensor = Tensor.fromBlob(buffer,
                    new long[]{BATCH_SIZE, 3, FRAME_WINDOW_SIZE, FACE_HEIGHT, FACE_WIDTH}, MemoryFormat.CONTIGUOUS);
            Tensor outputTensor = mPOSModule.forward(IValue.from(inputTensor)).toTensor();
            float[] outputData = outputTensor.getDataAsFloatArray();
            lastResult.HR_result = outputData[0];
            lastResult.RR_result = outputData[1];
            lastResult.spo2_result = outputData[2];
            lastResult.LF_HF_ratio = outputData[3];
            lastResult.sdnn_result = outputData[4];
            lastResult.SBP = outputData[5];
            lastResult.DBP = outputData[6];

            Log.d("Result", String.format("HR : %f, RR : %f, Spo2 : %f" +
                            "\nStress : %f, SDNN : %f, (SBP, DBP) : ( %f, %f)"
                    ,lastResult.HR_result, lastResult.RR_result, lastResult.spo2_result
                    ,lastResult.LF_HF_ratio, lastResult.sdnn_result, lastResult.SBP, lastResult.DBP));
            mUtcTimeTempList.clear();
        }
        return lastResult;
    }

    public static class Result {
        public float LF_HF_ratio = 0;
        public double HR_result = 0;
        public double RR_result = 0;
        public double spo2_result = 0;
        public double sdnn_result = 0;
        public double BP = 0;
        public double SBP = 0;
        public double DBP = 0;
    }

}
