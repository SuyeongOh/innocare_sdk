package com.innopia.vital_sync.service;

import com.innopia.vital_sync.data.ResultVitalSign;

public class GtRequest {
    public double hr = 0;
    public double rr = 0;
    public double spo2 = 0;
    public float stress = 0;
    public double hrv = 0;
    public double sbp = 0;
    public double dbp = 0;
    public long measureTime;
    public String id;

    public GtRequest(ResultVitalSign gt, long measureTime, String id) {
        this.hr = gt.HR;
        this.rr = gt.RR;
        this.spo2 = gt.SpO2;
        this.stress = gt.STRESS;
        this.hrv = gt.HRV;
        this.sbp = gt.SBP;
        this.dbp = gt.DBP;
        this.id = id;
        this.measureTime = measureTime;
    }
}
