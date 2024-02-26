package com.innopia.vital_sync.service;

public class VitalRequest {
    public double[][] RGB;
    public String id;

    public VitalRequest(double[][] RGB, String id) {
        this.RGB = RGB;
        this.id = id;
    }
}
