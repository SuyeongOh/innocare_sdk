package com.inniopia.funnylabs_sdk.data;

import android.graphics.ImageFormat;

import androidx.camera.core.CameraSelector;

public class Constant {
    public static final int IMAGE_BUFFER_SIZE = 3;
    public static final int PIXEL_FORMAT = ImageFormat.YUV_420_888;
    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;

    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;
    public static final int CAMERA_DIRECTION_FRONT = CameraSelector.LENS_FACING_FRONT;
    public static final int CAMERA_DIRECTION_BACK = CameraSelector.LENS_FACING_BACK;
    public static final String TYPE_OF_BIG = "big";
    public static final String TYPE_OF_SMALL = "small";
    public static final String TYPE_OF_OUT = "out";


}
