package com.innopia.vital_sync.analysis;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.gms.tasks.Tasks;
import com.google.android.odml.image.BitmapMlImageBuilder;
import com.google.android.odml.image.MlImage;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.common.InputImage;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static java.lang.Math.max;
import static java.lang.Math.min;

public abstract class VisionProcessorBase<T> implements VisionImageProcessor{

    protected static final String MANUAL_TESTING_LOG = "LogTagForTest";
    private static final String TAG = "VisionProcessorBase";

    private final ActivityManager activityManager;
    private final Timer fpsTimer = new Timer();
    private final ScopedExecutor executor;

    // Whether this processor is already shut down
    private boolean isShutdown;

    // Used to calculate latency, running in the same thread, no sync needed.
    private int numRuns = 0;
    private long totalFrameMs = 0;
    private long maxFrameMs = 0;
    private long minFrameMs = Long.MAX_VALUE;
    private long totalDetectorMs = 0;
    private long maxDetectorMs = 0;
    private long minDetectorMs = Long.MAX_VALUE;

    // Frame count that have been processed so far in an one second interval to calculate FPS.
    private int frameProcessedInOneSecondInterval = 0;
    private int framesPerSecond = 0;

    protected VisionProcessorBase(Context context) {
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        executor = new ScopedExecutor(TaskExecutors.MAIN_THREAD);
        fpsTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        framesPerSecond = frameProcessedInOneSecondInterval;
                        frameProcessedInOneSecondInterval = 0;
                    }
                },
                /* delay= */ 0,
                /* period= */ 1000);
    }

    // -----------------Code for processing single still image----------------------------------------
    @Override
    public void processBitmap(Bitmap bitmap) {
        long frameStartMs = SystemClock.elapsedRealtime();
        MlImage mlImage = new BitmapMlImageBuilder(bitmap).build();
        requestDetectInImage(mlImage,
                /* originalCameraImage= */ null,
                /* shouldShowFps= */ false, frameStartMs);
        mlImage.close();
    }

    // -----------------Common processing logic-------------------------------------------------------
    private Task<T> requestDetectInImage(final InputImage image, @Nullable final Bitmap originalCameraImage, boolean shouldShowFps, long frameStartMs) {
        return setUpListener(detectInImage(image), originalCameraImage, shouldShowFps, frameStartMs);
    }

    private Task<T> requestDetectInImage(final MlImage image, @Nullable final Bitmap originalCameraImage, boolean shouldShowFps, long frameStartMs) {
        return setUpListener(detectInImage(image), originalCameraImage, shouldShowFps, frameStartMs);
    }

    private Task<T> setUpListener(Task<T> task, @Nullable final Bitmap originalCameraImage, boolean shouldShowFps, long frameStartMs) {
        final long detectorStartMs = SystemClock.elapsedRealtime();
        return task.addOnSuccessListener(executor, results -> {
            long endMs = SystemClock.elapsedRealtime();
            long currentFrameLatencyMs = endMs - frameStartMs;
            long currentDetectorLatencyMs = endMs - detectorStartMs;
            if (numRuns >= 500) {
                resetLatencyStats();
            }
            numRuns++;
            frameProcessedInOneSecondInterval++;
            totalFrameMs += currentFrameLatencyMs;
            maxFrameMs = max(currentFrameLatencyMs, maxFrameMs);
            minFrameMs = min(currentFrameLatencyMs, minFrameMs);
            totalDetectorMs += currentDetectorLatencyMs;
            maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs);
            minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs);

            // Only log inference info once per second. When frameProcessedInOneSecondInterval is
            // equal to 1, it means this is the first frame processed during the current second.
            if (frameProcessedInOneSecondInterval == 1) {
                Log.d(TAG, "Num of Runs: " + numRuns);
                Log.d(TAG, "Frame latency: max=" + maxFrameMs + ", min=" + minFrameMs + ", avg=" + totalFrameMs / numRuns);
                Log.d(TAG, "Detector latency: max=" + maxDetectorMs + ", min=" + minDetectorMs + ", avg=" + totalDetectorMs / numRuns);
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(mi);
                long availableMegs = mi.availMem / 0x100000L;
                Log.d(TAG, "Memory available in system: " + availableMegs + " MB");
            }

            VisionProcessorBase.this.onSuccess(results);
        }).addOnFailureListener(executor, e -> {
            e.printStackTrace();
            VisionProcessorBase.this.onFailure(e);
        });
    }

    @Override
    public void stop() {
        executor.shutdown();
        isShutdown = true;
        resetLatencyStats();
        fpsTimer.cancel();
    }

    private void resetLatencyStats() {
        numRuns = 0;
        totalFrameMs = 0;
        maxFrameMs = 0;
        minFrameMs = Long.MAX_VALUE;
        totalDetectorMs = 0;
        maxDetectorMs = 0;
        minDetectorMs = Long.MAX_VALUE;
    }

    protected abstract Task<T> detectInImage(InputImage image);

    protected Task<T> detectInImage(MlImage image) {
        return Tasks.forException(new MlKitException("MlImage is currently not demonstrated for this feature", MlKitException.INVALID_ARGUMENT));
    }

    protected abstract void onSuccess(@NonNull T results);

    protected abstract void onFailure(@NonNull Exception e);

    protected boolean isMlImageEnabled(Context context) {
        return false;
    }
}