package com.vitalsync.vital_sync.utils;

import android.util.Log;

import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.data.Rppg;

import java.util.Arrays;


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
}
