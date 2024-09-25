package com.vitalsync.vital_sync.service.ecg;

public class EcgRequest {
    private String id;
    private int[] ecg_signal;
    private int[] ppg_signal;
    private long measurementTime;

    public EcgRequest(long time, String user_id){
        id = user_id;
        measurementTime = time;
    }

    public void setEcgSignal(int[] signal){
        ecg_signal = signal;
    }

    public void setPpgsignal(int[] signal){
        ppg_signal = signal;
    }
}
