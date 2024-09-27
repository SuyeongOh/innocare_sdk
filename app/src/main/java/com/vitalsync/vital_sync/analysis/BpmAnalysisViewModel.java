package com.vitalsync.vital_sync.analysis;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.vitalsync.vital_sync.data.ResultVitalSign;

public class BpmAnalysisViewModel extends AndroidViewModel {
    private Vital vital;
    private Context mContext;

    public BpmAnalysisViewModel(@NonNull Application application, Context context) {
        super(application);
        vital = new Vital();
        mContext = context;
    }

    public void addFaceImageModel(@NonNull FaceImageModel faceImageModel){
        vital.calculateVital(faceImageModel);
    }

    public void clearAnalysis(){
        vital.clearAnalysis();
    }
}
