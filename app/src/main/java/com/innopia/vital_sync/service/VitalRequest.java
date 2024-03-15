package com.innopia.vital_sync.service;

import com.innopia.vital_sync.data.ResultVitalSign;

public class VitalRequest {
    public double[][] RGB;
    public String measureTime;
    public String id;

    public VitalRequest(double[][] RGB, String measureTime, String id) {
        this.RGB = RGB;
        this.id = id;
        this.measureTime = measureTime;
    }
}
