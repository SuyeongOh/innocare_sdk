package com.innopia.vital_sync.video;

import com.innopia.vital_sync.data.VitalChartData;

public class CommonEvaluationMetrics {
    public String subject_id;
    public String slice_id;
    public double label_fft_hr;
    public double label_ibi_hr;
    public double label_ibi_mean;
    public double label_ibi_hrv;

    public static CommonEvaluationMetrics VitalSignToMetrics(){

    }

    public void setId(String id){
        subject_id = id;
    }

    public void setSlice(String id){
        slice_id = id;
    }
}
