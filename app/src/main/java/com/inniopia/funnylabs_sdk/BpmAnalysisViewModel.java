package com.inniopia.funnylabs_sdk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public class BpmAnalysisViewModel extends AndroidViewModel {
    private Vital vital;
    public BpmAnalysisViewModel(@NonNull Application application, Context context) {
        super(application);
//        try {
//            mTorchModule = LiteModuleLoader.load(
//                    FileUtils.assetFilePath(application, TORCH_MODULE_NAME));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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
