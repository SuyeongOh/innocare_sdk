package com.innopia.vital_sync.analysis;

import android.graphics.Bitmap;

import com.google.mlkit.common.MlKitException;

import java.nio.ByteBuffer;

import androidx.camera.core.ImageProxy;

/** An interface to process the images with different vision detectors and custom image models. */
public interface VisionImageProcessor {

    /** Processes a bitmap image. */
    void processBitmap(Bitmap bitmap);

    /** Stops the underlying machine learning model and release resources. */
    void stop();
}
