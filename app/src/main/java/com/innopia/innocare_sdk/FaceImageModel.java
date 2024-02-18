package com.innopia.innocare_sdk;

import android.graphics.Bitmap;

public class FaceImageModel {
    public Bitmap bitmap;
    public long frameUtcTimeMs;

    public FaceImageModel() {

    }

    public FaceImageModel(Bitmap bitmap, long frameUtcTimeMs) {
        this.bitmap = bitmap;
        this.frameUtcTimeMs = frameUtcTimeMs;
    }
}
