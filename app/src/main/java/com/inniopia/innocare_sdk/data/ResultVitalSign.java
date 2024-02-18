package com.inniopia.innocare_sdk.data;

import com.inniopia.innocare_sdk.VitalLagacy;

public class ResultVitalSign {
    //LF_HF_ratio는 stress 판별 value입니다. 3.0을 넘으면 스트레스가 많음/ 안넘으면 적절 지표로 저희 내부에선 생각하고 있습니다.
    public static ResultVitalSign vitalSignData = new ResultVitalSign();
    public float LF_HF_ratio = 0;
    public double HR_result = 0;
    public double RR_result = 0;
    public double spo2_result = 0;
    public double sdnn_result = 0;
    public double BP = 0;
    public double SBP = 0;
    public double DBP = 0;
    public float[] hr_array = new float[VitalLagacy.BPM_BUFFER_SIZE];
    public float[] rr_array = new float[VitalLagacy.BPM_BUFFER_SIZE];

    public void init(){
        LF_HF_ratio = 0;
        HR_result = 0;
        RR_result = 0;
        spo2_result = 0;
        sdnn_result = 0;
        BP = 0;
        SBP = 0;
        DBP = 0;
        hr_array = new float[VitalLagacy.BPM_BUFFER_SIZE];
        rr_array = new float[VitalLagacy.BPM_BUFFER_SIZE];
    }
}
