package com.vitalsync.vital_sync.data;

public class Config {
    public static final float THRESHOLD_DEFAULT = 0.5F;
    public static int USE_CAMERA_DIRECTION = Constant.CAMERA_DIRECTION_FRONT;
    public static int TARGET_FRAME = 30;
    public static int ANALYSIS_TIME = 20;


    public static final boolean FLAG_INNER_TEST = true;
    public static final boolean FLAG_VIDEO_TEST = false;

    public static final int FULL_SIZE_OF_WIDTH = 2736;
    public static final int FULL_SIZE_OF_HEIGHT = 3648;

    public static final double MIN_HR_FREQUENCY = 0.83f;
    public static final double MAX_HR_FREQUENCY = 2.5f;

    public static final double MIN_RR_FREQUENCY = 0.83f;
    public static final double MAX_RR_FREQUENCY = 2.5f;

    public static final double MIN_LF_FREQUENCY = 0.04f;
    public static final double MAX_LF_FREQUENCY = 0.15f;
    public static final double MIN_HF_FREQUENCY = 0.15f;
    public static final double MAX_HF_FREQUENCY = 0.4f;

    public static boolean LARGE_FACE_MODE = false;
    public static boolean SMALL_FACE_MODE = false;
    public static boolean SERVER_RESPONSE_MODE = false;
    public static final int FACE_MODEL_SIZE = 72;

    public static final String PRIVACY_POLICY = "https://sites.google.com/view/kwangkeelee-privacy/%ED%99%88";
    public static String LOCAL_SERVER_ADDRESS = "";
    public static final String CLOUD_SERVER_ADDRESS = "http://35.220.206.239";
    public static final String SERVER_PORT_HEADER = ":";
    public static final String SERVER_LOGIN_PORT = "3000";
    public static final String SERVER_VITAL_PORT = "1024";
    public static final String SERVER_PORT_FOOTER = "/";

    public static String USER_ID = "Guest";

    public static String Measure_Time;
    public static final String[] GT_LABEL_LIST = new String[]{
//            "HR", "RR", "HRV", "SpO2", "STRESS", "SBP", "DBP"
            "HR ", "HRV ", "RR ", "vital 1 ", "vital 2 ", "vital 3-1 ", "vital 3-2 "
    };

    public static final String[] RECORD_LABEL_LIST = new String[]{
            "HR", "RR", "HRV", "vital 1", "vital 2", "vital 3"
    };

    public static String USER_GENDER = "male";
    public static double USER_BMI = 0f;
    public static int USER_AGE = 0;
    public static double USER_HEIGHT = 0;
    public static double USER_WEIGHT = 0;
    public static double USER_SBP = 0;
    public static double USER_DBP = 0;
}

