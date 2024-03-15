package com.innopia.vital_sync.data;

public class ResultVitalSign {
    //LF_HF_ratio는 stress 판별 value입니다. 3.0을 넘으면 스트레스가 많음/ 안넘으면 적절 지표로 저희 내부에선 생각하고 있습니다.
    public static ResultVitalSign vitalSignData = new ResultVitalSign();
    public static ResultVitalSign vitalSignServerData = new ResultVitalSign();
    public double STRESS = 0;
    public double HR = 0;
    public double RR = 0;
    public double SpO2 = 0;
    public double HRV = 0;
    public double BP = 0;
    public double SBP = 0;
    public double DBP = 0;
    public double IBI_mean;
    public double IBI_HR;
    public void init(){
        STRESS = 0;
        HR = 0;
        RR = 0;
        SpO2 = 0;
        HRV = 0;
        BP = 0;
        SBP = 0;
        DBP = 0;
        IBI_mean = 0;
        IBI_HR = 0;
    }
}
