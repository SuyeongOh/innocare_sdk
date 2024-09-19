package com.vitalsync.vital_sync.service.ecg;

public class EcgRequest {
    private int[] signal;
    private String measureTime;

    public EcgRequest(int[] signal, String time){
        this.signal = signal;
        measureTime = String.valueOf(time);
    }
}
