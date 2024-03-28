package com.innopia.vital_sync.analysis;

import android.graphics.Bitmap;

public class FaceImageModel {
    public Bitmap bitmap;
    public long frameUtcTimeMs;
    public boolean isFinish;
    public FaceImageModel() {

    }

    public FaceImageModel(Bitmap bitmap, long frameUtcTimeMs, boolean isFinish) {
        this.bitmap = bitmap;
        this.frameUtcTimeMs = frameUtcTimeMs;
        this.isFinish = isFinish;
    }
}
