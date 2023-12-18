package com.inniopia.funnylabs_sdk;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class BpmAnalysisViewModel extends AndroidViewModel {
    private Vital vital;
    public BpmAnalysisViewModel(@NonNull Application application, Context context) {
        super(application);
        vital = new Vital(context);
    }

    public void addFaceImageModel(@NonNull FaceImageModel faceImageModel){
        calculateAnalysis(faceImageModel);
    }

    private void calculateAnalysis(@NonNull FaceImageModel faceImageModel){
        Vital.Result result = vital.calculatePOSVital(faceImageModel);

        //TODO result 이용해서 값 처리
    }
}
