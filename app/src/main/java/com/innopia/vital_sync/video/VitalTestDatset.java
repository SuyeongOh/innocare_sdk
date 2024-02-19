package com.innopia.vital_sync.video;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.innopia.vital_sync.BpmAnalysisViewModel;
import com.innopia.vital_sync.Config;
import com.innopia.vital_sync.FaceImageModel;
import com.innopia.vital_sync.Vital;
import com.innopia.vital_sync.VitalLagacy;
import com.innopia.vital_sync.utils.FileUtils;

public class VitalTestDatset {

    private Context mContext;

    private final String[] directoryList = new String[]{
            "pure"
    };

    public VitalTestDatset(Context context){
        mContext = context;
    }

    public void runTest(){
        if(mContext == null) {
            throw new RuntimeException("Context is Null");
        }
        AssetManager assetManager = mContext.getAssets();

        for(String dir : directoryList){
            try{
                String[] subfiles = assetManager.list(FileUtils.assetFilePath(mContext, dir));

                if(subfiles.length > 0){
                    for(String file : subfiles){
                        boolean isAnalysis = vitalTestVideo(Uri.parse(FileUtils.assetFilePath(mContext, dir + "/" + file)));
                        if(isAnalysis){
                            switch (dir){
                                case "pure":
                                    PureDataset pureDataset = new PureDataset(mContext);
                                    pureDataset.run(file);
                                    break;

                            }
                        }
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
                return ;
            }



        }
    }

    public boolean vitalTestVideo(Uri videoUri){
        BpmAnalysisViewModel bpmAnalysisViewModel = new BpmAnalysisViewModel(new Application(), mContext);
        try{
            videoUri = Uri.parse(FileUtils.assetFilePath(mContext, "ubfc_subject_1.mp4"));
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, videoUri);

        FaceImageModel faceImageModel = null;

        for(int i = 0; i < Config.ANALYSIS_TIME * Config.TARGET_FRAME; i++){
            try{
                Bitmap curFrame = retriever.getFrameAtIndex(i);
                faceImageModel = new FaceImageModel(curFrame, (long)i * 33);
                if(bpmAnalysisViewModel.addFaceImageModel(faceImageModel)){
                    return true;
                }
            } catch (Exception e){
                e.printStackTrace();
                Log.e("TestDataset", "Video is too short");
                return false;
            }
        }
        return false;
    }
}
