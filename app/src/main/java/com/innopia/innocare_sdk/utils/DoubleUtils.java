package com.innopia.innocare_sdk.utils;

public class DoubleUtils {
    public static double[] calculateH(double[][] S) {
        int length = S[0].length;
        double[] h = new double[length];
        double stdS0 = calculateStd(S[0]);
        double stdS1 = calculateStd(S[1]);
        double weight = stdS0 / stdS1;

        for (int i = 0; i < length; i++) {
            h[i] = S[0][i] + weight * S[1][i];
        }
        return h;
    }

    public static double calculateStd(double[] data) {
        double mean = calculateMean(data);
        double sum = 0;
        for (double value : data) {
            sum += Math.pow(value - mean, 2);
        }
        return Math.sqrt(sum / data.length);
    }

    public static double calculateMean(double[] data) {
        double sum = 0;
        for (double value : data) {
            sum += value;
        }
        return sum / data.length;
    }
}