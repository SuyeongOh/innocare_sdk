package com.vitalsync.vital_sync.camera;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.video.Tracker;
import org.opencv.video.TrackerMIL;

public class FaceTracker {
    private Tracker mTracker;
    private Rect boundingBox;

    public FaceTracker(Bitmap img, RectF bBox){
        if(OpenCVLoader.initLocal()){
            Log.d("OpenCV", "OpenCV Load Successfully !!");
        } else{
            Log.d("OpenCV", "OpenCV Load Failed !!");
        }
        Mat imgMat = new Mat();
        boundingBox = new Rect((int) bBox.left, (int) bBox.top, (int) bBox.width(), (int) bBox.height());

        Utils.bitmapToMat(img, imgMat);
        mTracker = TrackerMIL.create();

        mTracker.init(imgMat, boundingBox);
    }

    public RectF update(Bitmap img){
        Mat imgMat = new Mat();
        Utils.bitmapToMat(img, imgMat);

        if(mTracker.update(imgMat, boundingBox)){
            Log.d("OpenCV", "Update Tracker !!");
            return new RectF(
                    boundingBox.x
                    , boundingBox.y
                    , boundingBox.x+boundingBox.width
                    , boundingBox.y+boundingBox.height
            );
        } else{
            return null;
        }
    }
}
