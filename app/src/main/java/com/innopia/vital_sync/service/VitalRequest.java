package com.innopia.vital_sync.service;

public class VitalRequest {
    public double[][] RGB;

    public double bmi;
    public int age;
    public String gender;
    public String measureTime;
    public String id;

    public VitalRequest(double[][] RGB, double bmi, int age, String gender, String measureTime, String id) {
        this.RGB = RGB;
        this.id = id;
        this.measureTime = measureTime;
        this.bmi = bmi;
        this.age = age;
        this.gender = gender.equals("male") ? "male" : "female";
    }
}
