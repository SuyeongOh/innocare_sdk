package com.vitalsync.vital_sync.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtils {
    // 2000년 1월 1일을 기준으로 한 epoch 타임스탬프 계산
    public static long getEpochTimeStamp_2000_1_1(){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getCurrentTimeStamp_by_2000_1_1(long start_time){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long timestamp_2000_1_1 = calendar.getTimeInMillis();
        return start_time - timestamp_2000_1_1;
    }
}
