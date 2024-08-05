package com.vitalsync.vital_sync.utils;

import android.util.Log;

import com.paramsen.noise.Noise;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.data.Rppg;

import org.apache.commons.math3.util.MathArrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class RppgUtils {
    public static int TimeToLen(double seconds, long[] eventTimes) {
        if (eventTimes == null || eventTimes.length == 0) {
            return 1; // Return 1 if the input list is null or empty
        }

        long firstEventTime = eventTimes[0];
        for (int index = 0; index < eventTimes.length; index++) {
            if (eventTimes[index] - firstEventTime > seconds * 1000) { // 1.0 초 차이를 밀리초 단위로 계산
                return index;
            }
        }
        return 1;
    }

    public static int[] PeakDetect(double[] signal, Rppg rppg, double bpm) {
        int windowSize = TimeToLen(1.0, rppg.frameTimeArray);

        windowSize = (windowSize * 60) / (int)bpm;

        int[] peakArray = new int[signal.length];

        int peakIndex = 0;
        while(peakIndex < (signal.length - windowSize)) {
            for (int i = peakIndex; i < Math.min(peakIndex + windowSize, signal.length); i++) {
                if (signal[i] > signal[peakIndex]) {
                    peakIndex = i;
                }
            }
            peakArray[peakIndex] = 1;

            for (int i = peakIndex; i < Math.min(peakIndex + windowSize, signal.length); i++) {
                if (signal[i] < signal[peakIndex]) {
                    peakIndex = i;
                }
            }
            peakArray[peakIndex] = 2;
        }

        return peakArray;
    }

    public static long[] interpolateTime(long[] timeArray, int fps){

        long startTime = timeArray[0];
        long endTime = 0;

        int dataLen = Config.ANALYSIS_TIME * Config.TARGET_FRAME;;
        for(int i = 0; i < timeArray.length; i++){
            if(timeArray[i] == 0){
                endTime = timeArray[i-1];
                break;
            }
        }

        if(endTime == 0){
            endTime = timeArray[timeArray.length-1];
        }

        long[] interpolatedArray = new long[dataLen];
        for (int i = 0; i < dataLen; i++) {
            long value = (long)((endTime - startTime) * i / (double)dataLen) + startTime;
            interpolatedArray[i] = value;
        }

        return interpolatedArray;
    }

    public static double[][] interpolateSignal(double[][] rgb, long[] originalTimeArray, long[] procTimeArray, int fps){
        double[][] newSignal = new double[rgb.length][procTimeArray.length];

        newSignal[0][0] = rgb[0][0];
        newSignal[1][0] = rgb[1][0];
        newSignal[2][0] = rgb[2][0];

        if(fps < 25){
            //target frame : 30
            //0 출현 전까지 데이터갯수 -> 600개 샘플링
            int originalIdx = 1;
            for(int i = 1; i < newSignal[0].length; i++){
                if(procTimeArray[i] >= originalTimeArray[originalIdx]){
                    originalIdx++;
                }
                long prevTime = originalTimeArray[originalIdx-1];

                double prevR = rgb[0][originalIdx-1];
                double prevG = rgb[1][originalIdx-1];
                double prevB = rgb[2][originalIdx-1];

                double nextR = rgb[0][originalIdx];
                double nextG = rgb[1][originalIdx];
                double nextB = rgb[2][originalIdx];

                double gradR = nextR - prevR;
                double gradG = nextG - prevG;
                double gradB = nextB - prevB;

                double weight = (procTimeArray[i] - prevTime) / (double)(originalTimeArray[originalIdx] - prevTime);


                newSignal[0][i] = prevR + gradR * weight;
                newSignal[1][i] = prevG + gradG * weight;
                newSignal[2][i] = prevB + gradB * weight;
            }

            return newSignal;
        }
        for (int i = 1; i < rgb[0].length; i++) {

            double v1 = originalTimeArray[i] - originalTimeArray[i-1];
            double v2 = procTimeArray[i] - originalTimeArray[i-1];

            double gradient = v2/v1;

            newSignal[0][i] = ((rgb[0][i] - rgb[0][i - 1]) * gradient) + rgb[0][i - 1];
            newSignal[1][i] = ((rgb[1][i] - rgb[1][i - 1]) * gradient) + rgb[1][i - 1];
            newSignal[2][i] = ((rgb[2][i] - rgb[2][i - 1]) * gradient) + rgb[2][i - 1];
        }
        return newSignal;
    }


    private static double[] blackmanWindow(int length) {

        double[] window = new double[length];
        double factor = Math.PI / (length - 1);

        for (int i = 0; i < window.length; ++i) {
            window[i] = 0.42d - (0.5d * Math.cos(2 * factor * i)) + (0.08d * Math.cos(4 * factor * i));
        }

        return window;
    }

    public static double[] lowPassKernel(int length, double cutoffFreq, double[] window) {

        double[] ker = new double[length + 1];
        double factor = Math.PI * cutoffFreq * 2;
        double sum = 0;

        for (int i = 0; i < ker.length; i++) {
            double d = i - length / 2;
            if (d == 0) ker[i] = factor;
            else ker[i] = Math.sin(factor * d) / d;
            ker[i] *= window[i];
            sum += ker[i];
        }

        // Normalize the kernel
        for (int i = 0; i < ker.length; ++i) {
            ker[i] /= sum;
        }

        return ker;
    }

    public static double[] bandPassKernel(int length, double lowFreq, double highFreq) {

        double[] ker = new double[length + 1];
        double[] window = blackmanWindow(length + 1);

        // Create a band reject filter kernel using a high pass and a low pass filter kernel
        double[] lowPass = lowPassKernel(length, lowFreq, window);

        // Create a high pass kernel for the high frequency
        // by inverting a low pass kernel
        double[] highPass = lowPassKernel(length, highFreq, window);
        for (int i = 0; i < highPass.length; ++i) highPass[i] = -highPass[i];
        highPass[length / 2] += 1;

        // Combine the filters and invert to create a bandpass filter kernel
        for (int i = 0; i < ker.length; ++i) ker[i] = -(lowPass[i] + highPass[i]);
        ker[length / 2] += 1;

        return ker;
    }

    public static double[] filter(double[] signal, double[] kernel) {

        double[] res = new double[signal.length];

        for (int r = 0; r < res.length; ++r) {

            int M = Math.min(kernel.length, r + 1);
            for (int k = 0; k < M; ++k) {
                res[r] += kernel[k] * signal[r - k];
            }
        }

        return res;
    }

    public int HSV_fft(Noise noise2, float[] frame_hue_avg, float[] fft_hue, int HUE_FRAME, boolean[] hue_filter) {

        noise2.fft(frame_hue_avg, fft_hue);

        fft_hue[0] = fft_hue[1] = 0;

        for (int i = 0; i < HUE_FRAME / 2; i++) {
            fft_hue[i * 2 + 2] = fft_hue[i * 2 + 2] * (hue_filter[i] ? 1.0f : 0.0f);
        }

        int hue_hr_index = 0;
        float hue_max = 0.0f;

        for (int i = 0; i < HUE_FRAME / 2; i++) {
            if (fft_hue[i * 2 + 2] > hue_max) {
                hue_max = fft_hue[i * 2 + 2];
                hue_hr_index = i;
            }
        }

        return hue_hr_index;
    }

    public static ArrayList<Integer> intersect1d(ArrayList<Integer> arr1, ArrayList<Integer> arr2) {
        // 중복 제거를 위한 HashSet 사용
        HashSet<Integer> set1 = new HashSet<>(arr1);
        HashSet<Integer> set2 = new HashSet<>(arr2);

        // 교집합 요소를 저장할 ArrayList
        ArrayList<Integer> intersectionList = new ArrayList<>();

        // 교집합 찾기
        for (Integer element : set1) {
            if (set2.contains(element)) {
                intersectionList.add(element);
            }
        }
        return intersectionList;
    }

    public static Map<Integer, Integer> closestPairs(ArrayList<Integer> arr1, ArrayList<Integer> arr2) {
        HashSet<Integer> usedArr1 = new HashSet<>();
        HashSet<Integer> usedArr2 = new HashSet<>();
        Map<Integer, Integer> result = new HashMap<>();

        for (int k = 0; k < Math.min(arr1.size(), arr2.size()); k++) {
            int minDiff = Integer.MAX_VALUE;
            int key = -1;
            int value = -1;

            for (int i = 0; i < arr1.size(); i++) {
                if (usedArr1.contains(i)) {
                    continue;
                }
                for (int j = 0; j < arr2.size(); j++) {
                    if (usedArr2.contains(j)) {
                        continue;
                    }
                    int diff = Math.abs(arr1.get(i) - arr2.get(j));
                    if (diff < minDiff) {
                        minDiff = diff;
                        key = arr1.get(i);
                        value = arr2.get(j);
                    }
                }
            }
            if (key != -1 && value != -1) {
                usedArr1.add(key);
                usedArr2.add(value);
                result.put(key, value);
            }
        }
        return result;
    }

    public static double getSpO2RoR(double[] log_HbO2, double[] log_Hb) {
        //RoR = median
        double[] divide_hbo2_hb = MathArrays.ebeDivide(log_HbO2, log_Hb);

        Arrays.sort(divide_hbo2_hb);
        double median = 0;
        if(divide_hbo2_hb.length %2 == 0){
            median = (divide_hbo2_hb[divide_hbo2_hb.length/2] + divide_hbo2_hb[divide_hbo2_hb.length/2 - 1])/2;
        } else {
            median = divide_hbo2_hb[divide_hbo2_hb.length/2];
        }

        if(median > 1){
            return median;
        }

        divide_hbo2_hb = MathArrays.ebeDivide(log_Hb, log_HbO2);
        Arrays.sort(divide_hbo2_hb);
        if(divide_hbo2_hb.length %2 == 0){
            median = (divide_hbo2_hb[divide_hbo2_hb.length/2] + divide_hbo2_hb[divide_hbo2_hb.length/2 - 1])/2;
        } else {
            median = divide_hbo2_hb[divide_hbo2_hb.length/2];
        }

        return median;
    }
}
