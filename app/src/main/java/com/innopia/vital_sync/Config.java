package com.innopia.vital_sync;

import com.innopia.vital_sync.data.Constant;

public class Config {
    public static final float THRESHOLD_DEFAULT = 0.5F;
    public static int CURRENT_DELEGATE = Constant.DELEGATE_CPU;
    public static int USE_CAMERA_DIRECTION = Constant.CAMERA_DIRECTION_FRONT;
    public static int TARGET_FRAME = 30;
    public static int ANALYSIS_TIME = 20;
    public static int SCREEN_WIDTH = 0;
    public static int SCREEN_HEIGHT = 0;
    public static double USER_BMI = 0f;

    public static final boolean FLAG_INNER_TEST = true;
    public static final boolean FLAG_VIDEO_TEST = false;

    public static final int FULL_SIZE_OF_WIDTH = 2736;
    public static final int FULL_SIZE_OF_HEIGHT = 3648;

    public static final double MIN_HR_FREQUENCY = 0.83f;
    public static final double MAX_HR_FREQUENCY = 2.5f;

    public static final double MIN_RR_FREQUENCY = 0.83f;
    public static final double MAX_RR_FREQUENCY = 2.5f;
    public static boolean LARGE_FACE_MODE = false;

    public static final int FACE_MODEL_SIZE = 72;

    public static String LOCAL_SERVER_ADDRESS = "http://172.168.50.59:1004/";
}
