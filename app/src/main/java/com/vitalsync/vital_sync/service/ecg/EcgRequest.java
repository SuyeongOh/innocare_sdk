package com.vitalsync.vital_sync.service.ecg;

public class EcgRequest {
    private int[] ecg_signal;
    private int[] ppg_signal;
    private long measureTime;

    public EcgRequest(long time){
        measureTime = time;
    }

    public void setEcgSignal(int[] signal){
        ecg_signal = signal;
    }

    public void setPpgsignal(int[] signal){
        ppg_signal = signal;
    }
}
