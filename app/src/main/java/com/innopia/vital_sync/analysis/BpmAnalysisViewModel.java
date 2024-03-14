package com.innopia.vital_sync.analysis;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.innopia.vital_sync.data.ResultVitalSign;

public class BpmAnalysisViewModel extends AndroidViewModel {
    private Vital vital;
    private Context mContext;

    public BpmAnalysisViewModel(@NonNull Application application, Context context) {
        super(application);
        vital = new Vital();
        mContext = context;
    }

    public boolean addFaceImageModel(@NonNull FaceImageModel faceImageModel){
        return calculateAnalysis(faceImageModel);
    }

    private boolean calculateAnalysis(@NonNull FaceImageModel faceImageModel){
        ResultVitalSign.vitalSignData = Vital.toResultVitalSign(vital.calculateVital(faceImageModel));
        return ResultVitalSign.vitalSignData.SBP != 0;
    }

    public void clearAnalysis(){
        vital.clearAnalysis();
    }
}
