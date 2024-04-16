package com.vitalsync.vital_sync.video;

import com.vitalsync.vital_sync.data.VitalChartData;

public class CommonEvaluationMetrics {
    public String subject_id;
    public String slice_id;
    public double label_fft_hr;
    public double label_ibi_hr;
    public double label_ibi_mean;
    public double label_ibi_hrv;

    public void setId(String id){
        subject_id = id;
    }

    public void setSlice(String id){
        slice_id = id;
    }

    public String[] getValueStringArray(){
        return new String[]{
                Double.toString(label_fft_hr),
                Double.toString(label_ibi_hr),
                Double.toString(label_ibi_mean),
                Double.toString(label_ibi_hrv),
        };
    }
}
