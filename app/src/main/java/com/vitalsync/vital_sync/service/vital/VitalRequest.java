package com.vitalsync.vital_sync.service.vital;

import com.vitalsync.vital_sync.data.Config;

public class VitalRequest {
    public double[][] RGB;

    public double bmi;
    public double height;
    public double weight;
    public int age;
    public String gender;
    public String measureTime;
    public String id;

    public VitalRequest(double[][] RGB, String measureTime, String id) {
        this.RGB = RGB;
        this.id = id;
        this.measureTime = measureTime;
        this.height = Config.USER_HEIGHT;
        this.weight = Config.USER_WEIGHT;
        this.bmi = Config.USER_BMI;
        this.age = Config.USER_AGE;
        this.gender = Config.USER_GENDER.equals("male") ? "male" : "female";
    }
}
