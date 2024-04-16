package com.vitalsync.vital_sync.video;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.vitalsync.vital_sync.analysis.BpmAnalysisViewModel;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.analysis.FaceImageModel;
import com.vitalsync.vital_sync.utils.FileUtils;

import java.io.File;

public class VitalTestDataset {

    private Context mContext;

    private final String[] directoryList = new String[]{
            "pure"
    };

    public VitalTestDataset(Context context){
        mContext = context;
    }

    public void runTest(){
        if(mContext == null) {
            throw new RuntimeException("Context is Null");
        }
        AssetManager assetManager = mContext.getAssets();

        for(String dir : directoryList){
            try{
                String[] subfiles = FileUtils.getFileListFromAssets(mContext, dir);
                File dirFile = new File(mContext.getFilesDir() + "/" + dir);
                if(dirFile.mkdir()) Log.d("Vital", "New Directory :: " + dir);
                if(subfiles.length > 0){
                    for(String file : subfiles){
                        if(file.contains(".csv")) continue;
                        boolean isAnalysis = vitalTestVideo(Uri.parse(FileUtils.assetFilePath(mContext, dir + "/" + file)));
                        Log.d("vital", "file name :: " + dir + "/" + file);
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

    private boolean vitalTestVideo(Uri videoUri){
        BpmAnalysisViewModel bpmAnalysisViewModel = new BpmAnalysisViewModel(new Application(), mContext);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, videoUri);

        FaceImageModel faceImageModel = null;

        for(int i = 0; i < Config.ANALYSIS_TIME * Config.TARGET_FRAME; i++){
            try{
                Bitmap curFrame = retriever.getFrameAtIndex(i);
                faceImageModel = new FaceImageModel(curFrame, (long)(i * 1000L /(float)30), i==600);
                bpmAnalysisViewModel.addFaceImageModel(faceImageModel);
                if(i == 600){
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
