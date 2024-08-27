package com.vitalsync.vital_sync.service.ecg;

public class EcgRequest {
    private int[] ecgSignal;
    private String measureTime;

    public EcgRequest(int[] signal, long time){
        ecgSignal = signal;
        measureTime = String.valueOf(time);
    }
}
