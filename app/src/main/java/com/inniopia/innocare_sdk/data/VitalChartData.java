package com.inniopia.innocare_sdk.data;

public class VitalChartData {
    public static long[] frameTimeArray;
    public static float FREQUENCY_INTERVAL;
    public static int FRAME_RATE;
    public static int START_FILTER_INDEX = 0;
    public static double[] R_SIGNAL;
    public static double[] G_SIGNAL;
    public static double[] B_SIGNAL;
    public static double[] SMOOTH_R_SIGNAL;
    public static double[] SMOOTH_G_SIGNAL;
    public static double[] SMOOTH_B_SIGNAL;
    public static double[] CORE_SIGNAL;
    public static double[] DETREND_SIGNAL;
    public static double[] BPF_SIGNAL;
    public static double[] BVP_SIGNAL;
    public static double[] FFT_SIGNAL;
    public static double[] HR_SIGNAL;
    public static int[] HRV_PEAK;
}
