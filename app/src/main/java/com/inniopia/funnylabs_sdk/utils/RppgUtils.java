package com.inniopia.funnylabs_sdk.utils;

import com.inniopia.funnylabs_sdk.VitalLagacy;
import com.inniopia.funnylabs_sdk.data.Rppg;

public class RppgUtils {
    public static int TimeToLen(double seconds, long[] eventTimes) {
        if (eventTimes == null || eventTimes.length == 0) {
            return 1; // Return 1 if the input list is null or empty
        }

        double firstEventTime = eventTimes[0];
        for (int i = 0; i < eventTimes.length; i++) {
            if (eventTimes[i] - firstEventTime > seconds) {
                return i;
            }
        }
        return 1; // Return 1 if no event meets the criteria
    }

    public static int[] PeakDetect(double[] green_signal, Rppg rppg, double bpm) {
        int windowSize = TimeToLen(1.0, rppg.frameTimeArray);

        windowSize = (windowSize * 60) / (int)bpm;

        int[] peakArray = new int[green_signal.length - windowSize];

        int peakIndex = 0;

        while (peakIndex < (green_signal.length - windowSize)) {
            for (int i = peakIndex; i < Math.min(peakIndex + windowSize, green_signal.length); i++) {
                if (green_signal[i] > green_signal[peakIndex]) {
                    peakIndex = i;
                }
            }
            peakArray[peakIndex] = 1;

            for (int i = peakIndex; i < Math.min(peakIndex + windowSize, green_signal.length); i++) {
                if (green_signal[i] < green_signal[peakIndex]) {
                    peakIndex = i;
                }
            }
            peakArray[peakIndex] = 2;
        }
        return peakArray;
    }

}
