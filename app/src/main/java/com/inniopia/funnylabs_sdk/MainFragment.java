package com.inniopia.funnylabs_sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;
import com.inniopia.funnylabs_sdk.data.ResultVitalSign;
import com.inniopia.funnylabs_sdk.ui.CommonPopupView;
import com.robinhood.ticker.TickerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainFragment extends Fragment implements EnhanceFaceDetector.DetectorListener {
    private static final boolean FLAG_INNER_TEST = true;

    //View Variable
    private PreviewView mCameraView;
    private OverlayView mTrackingOverlayView;
    private ProgressBar mProgressBar;
    private BpmAnalysisViewModel mBpmAnalysisViewModel;
    private CommonPopupView mGuidePopupView;
    private AlertDialog mFinishPopup;
    private TextView mGuidePopupText;
    private LineChart mBvpChart;
    private LineChart mGreenChart;
    private View mVitalGroup;
    private TickerView hrValueView;
    private TickerView rrValueView;
    private TickerView sdnnValueView;
    private TickerView spo2ValueView;
    private TickerView stressValueView;
    private TickerView sbpValueView;
    private TickerView dbpValueView;

    private View vitalValueLayout;
    private LineDataSet mBvpDataset;
    private LineDataSet mGreenData;

    //Camera Property variable
    private EnhanceFaceDetector faceDetector;
    private ExecutorService mFrontCameraExecutor;
    private ProcessCameraProvider mCameraProvider;
    private Preview mPreview;
    private ImageAnalysis mImageAnalysis;
    private Camera mCamera;
    public Bitmap mOriginalBitmap;
    private long lastFrameUtcTimeMs = -1;

    private int CAMERA_RATIO = AspectRatio.RATIO_16_9;
    private int ROTATION = Surface.ROTATION_0;

    private int sNthFrame = 0;

    private boolean isStopPredict = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBpmAnalysisViewModel = new BpmAnalysisViewModel(new Application(), requireContext());

        mFrontCameraExecutor = Executors.newSingleThreadExecutor();

        mFrontCameraExecutor.execute(
                () -> {
                    faceDetector = new EnhanceFaceDetector(requireContext(), this);
                    faceDetector.setupFaceDetector();
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Face Detection
        mCameraView = view.findViewById(R.id.view_finder);
        mTrackingOverlayView = view.findViewById(R.id.tracking_overlay);
        mProgressBar = view.findViewById(R.id.progress);


        if(FLAG_INNER_TEST){
            mVitalGroup = view.findViewById(R.id.vital_info_group);
            mVitalGroup.setVisibility(View.VISIBLE);
            mBvpChart = view.findViewById(R.id.bvp_chart);
            mGreenChart = view.findViewById(R.id.green_chart);
            initChart();
        }

        //Face Guide Popup
        View viewNoDetectionPopup = inflater.inflate(R.layout.layout_detection_popup, container, false);
        mGuidePopupView = new CommonPopupView(requireContext(),viewNoDetectionPopup);
        mGuidePopupText = viewNoDetectionPopup.findViewById(R.id.text_face_popup);
        mGuidePopupText.setText(R.string.face_no_detection);

        vitalValueLayout = view.findViewById(R.id.vital_info_layout);
        hrValueView = view.findViewById(R.id.heart_rate_value);
        rrValueView = view.findViewById(R.id.respiratory_rate_value);
        sdnnValueView = view.findViewById(R.id.hrv_sdnn_value);
        spo2ValueView = view.findViewById(R.id.oxygen_saturation_value);
        stressValueView = view.findViewById(R.id.stress_value);
        sbpValueView = view.findViewById(R.id.highest_blood_pressure_value);
        dbpValueView = view.findViewById(R.id.lowest_blood_pressure_value);

        mFinishPopup = new AlertDialog.Builder(getContext())
                .setTitle("분석 마침")
                .setMessage("결과화면으로 넘어가시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getContext(), ResultActivity.class);
                        getContext().startActivity(intent);
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mCameraView.post(new Runnable() {
            @Override
            public void run() {
                setUpCamera(mCameraView.getSurfaceProvider());
            }
        });
    }

    private void initChart(){
        mBvpChart.getDescription().setText("BVP");
        mBvpChart.getDescription().setEnabled(true);
        Legend legend = mBvpChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(10);
        legend.setTextSize(13);
        legend.setTextColor(Color.parseColor("#A3A3A3"));
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        legend.setYEntrySpace(5);

        // XAxis (아래쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis = mBvpChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 데이터 표시 위치
        xAxis.setGranularity(1f);
        xAxis.setTextSize(14f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setSpaceMin(0.1f); // Chart 맨 왼쪽 간격 띄우기
        xAxis.setSpaceMax(0.1f); // Chart 맨 오른쪽 간격 띄우기

        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxisLeft = mBvpChart.getAxisLeft();
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setTextColor(Color.BLACK);
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setAxisLineWidth(2);
        yAxisLeft.setAxisMinimum(0f); // 최솟값

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxis = mBvpChart.getAxisRight();
        yAxis.setDrawLabels(false); // label 삭제
        yAxis.setTextColor(Color.BLACK);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisLineWidth(2);
        yAxis.setAxisMinimum(0f); // 최솟값
        yAxis.setAxisMaximum((float) 255); // 최댓값
        yAxis.setGranularity((float) 255);

        mGreenChart.getDescription().setText("G signal");
        mGreenChart.getDescription().setEnabled(true);
        Legend legend_g = mGreenChart.getLegend();
        legend_g.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend_g.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend_g.setForm(Legend.LegendForm.CIRCLE);
        legend_g.setFormSize(10);
        legend_g.setTextSize(13);
        legend_g.setTextColor(Color.parseColor("#A3A3A3"));
        legend_g.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend_g.setDrawInside(false);
        legend_g.setYEntrySpace(5);

        // XAxis (아래쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis_g = mGreenChart.getXAxis();
        xAxis_g.setDrawAxisLine(false);
        xAxis_g.setDrawGridLines(false);
        xAxis_g.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 데이터 표시 위치
        xAxis_g.setGranularity(1f);
        xAxis_g.setTextSize(14f);
        xAxis_g.setTextColor(Color.BLACK);
        xAxis_g.setSpaceMin(0.1f); // Chart 맨 왼쪽 간격 띄우기
        xAxis_g.setSpaceMax(0.1f); // Chart 맨 오른쪽 간격 띄우기

        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxisLeft_g = mGreenChart.getAxisLeft();
        yAxisLeft_g.setDrawAxisLine(false);

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxis_g = mGreenChart.getAxisRight();
        yAxis_g.setDrawLabels(false); // label 삭제
        yAxis_g.setTextColor(Color.BLACK);
        yAxis_g.setDrawAxisLine(false);
        yAxis_g.setAxisLineWidth(2);
        yAxis_g.setAxisMinimum(0f); // 최솟값
        yAxis_g.setAxisMaximum((float) 512); // 최댓값
        yAxis_g.setGranularity((float) 512);
    }

    private void setUpCamera(Preview.SurfaceProvider surfaceProvider){
        ListenableFuture<ProcessCameraProvider> cameraProvider
                = ProcessCameraProvider.getInstance(requireContext());

        cameraProvider.addListener(new Runnable() {
            @Override
            public void run() {
                try{
                    mCameraProvider = cameraProvider.get();
                    bindCameraUseCases(surfaceProvider);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindCameraUseCases(Preview.SurfaceProvider surfaceProvider){
        ProcessCameraProvider cameraProvider = mCameraProvider;
        CameraSelector cameraSelector =
                new CameraSelector.Builder().requireLensFacing(Config.USE_CAMERA_DIRECTION).build();

        mPreview = new Preview.Builder()
                .setTargetAspectRatio(CAMERA_RATIO)
                .setTargetRotation(ROTATION)
                .build();

        mImageAnalysis = new ImageAnalysis.Builder().setTargetAspectRatio(CAMERA_RATIO)
                 .setTargetRotation(ROTATION)
                 .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                 .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                 .build();

        mImageAnalysis.setAnalyzer(
                mFrontCameraExecutor, faceDetector::detectLiveStreamFrame);
        cameraProvider.unbindAll();

        try{
            mCamera = cameraProvider.bindToLifecycle(this, cameraSelector, mPreview, mImageAnalysis);
            mPreview.setSurfaceProvider(surfaceProvider);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private Runnable postInferenceCallback;

    public void processImage(MPImage image, EnhanceFaceDetector.ResultBundle resultBundle){
        postInferenceCallback = image::close;
        if(sNthFrame > Vital.BATCH_SIZE * Vital.FRAME_WINDOW_SIZE){
            return;
        }
        boolean isFinishAnalysis = false;
        List<FaceDetectorResult> faceDetectorResults = resultBundle.getResults();

        FaceImageModel faceImageModel = null;

        try {
            if (faceDetectorResults.get(0).detections().size() >= 1) {
                RectF box = faceDetectorResults.get(0).detections().get(0).boundingBox();
                List<NormalizedKeypoint> facePoints = faceDetectorResults.get(0).detections().get(0).keypoints().get();

                if(mTrackingOverlayView.isOutBoundary(box)){
                    if(!isStopPredict) {
                        stopPrediction(Config.TYPE_OF_OUT);
                    }
                    readyForNextImage();
                    return;
                } else if(mTrackingOverlayView.isBigSize(box)){
                    if(!isStopPredict) {
                        stopPrediction(Config.TYPE_OF_BIG);
                    }
                    readyForNextImage();
                    return;
                } else if (mTrackingOverlayView.isSmallSize(box)) {
                    if(!isStopPredict) {
                        stopPrediction(Config.TYPE_OF_SMALL);
                    }
                    readyForNextImage();
                    return;
                }
                isStopPredict = false;
                mGuidePopupView.dismiss();

                int width = image.getWidth();
                int height = image.getHeight();
                float left = facePoints.get(4).x() * width;
                float right = facePoints.get(5).x() * width;
                float top = Math.max(facePoints.get(4).y(), facePoints.get(5).y()) * height;
                float bottom = facePoints.get(3).y() * height;
                RectF rectF = new RectF(left, top, right, bottom);

                Rect rect = new Rect();
                box.round(rect);

                Log.d("jupiter", String.format("%d Frame Box : (%d, %d, %d, %d", sNthFrame, rect.left, rect.top, rect.right, rect.bottom));
                Bitmap croppedFaceBitmap = Bitmap.createBitmap(mOriginalBitmap, rect.left, rect.top, rect.width(), rect.height());
                faceImageModel = new FaceImageModel(croppedFaceBitmap, getLastFrameUtcTimeMs());
                Log.d("Result", "Nth Frame : " + sNthFrame);
                sNthFrame ++;
                isFinishAnalysis = mBpmAnalysisViewModel.addFaceImageModel(faceImageModel);
                if(FLAG_INNER_TEST){
                    Vital vital = mBpmAnalysisViewModel.getVital();
                    VitalLagacy lagacy = vital.getVitalLagacy();

                    HandlerThread thread_g = new HandlerThread("G signal Thread");
                    HandlerThread thread_bvp = new HandlerThread("bvp signal Thread");
                    thread_g.start();
                    thread_bvp.start();
                    new Handler(thread_g.getLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            List<Entry> entryList = new ArrayList<>();
                            double[] g_signal = lagacy.getGreenSignal();
                            for(int i = 0; i < g_signal.length; i++){
                                entryList.add(new Entry(i, (float)g_signal[i]));
                            }
                            mGreenData = new LineDataSet(entryList, "");
                            mGreenData.setDrawCircles(false);
                            mGreenData.setColor(Color.CYAN);
                            LineData data = new LineData(mGreenData);
                            mGreenChart.setData(data);
                            mGreenChart.notifyDataSetChanged();
                            mGreenChart.invalidate();
                        }
                    });

                    if(sNthFrame % VitalLagacy.BPM_CALCULATION_FREQUENCY == 0){
                        new Handler(thread_bvp.getLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                List<Entry> entryList = new ArrayList<>();
                                double[] bvp_signal = lagacy.getBvpSignal();
                                for(int i = 0; i < bvp_signal.length; i++){
                                    entryList.add(new Entry(i, (float)bvp_signal[i]));
                                }
                                mBvpDataset = new LineDataSet(entryList, "");
                                mBvpDataset.setDrawCircles(false);
                                mBvpDataset.setColor(Color.MAGENTA);
                                LineData data = new LineData(mBvpDataset);
                                mBvpChart.setData(data);
                                mBvpChart.notifyDataSetChanged();
                                mBvpChart.invalidate();
                            }
                        });
                        updateVitalSignValue();
                    }
                }
            }
            if (mTrackingOverlayView != null) {
                FaceDetectorResult result = resultBundle.getResults().get(0);
                mTrackingOverlayView.setResults(result,
                        resultBundle.inputImageWidth,
                        resultBundle.inputImageHeight);
                mTrackingOverlayView.invalidate();
            }
            if(mProgressBar.getProgress() != (sNthFrame/(Vital.BATCH_SIZE * Vital.FRAME_WINDOW_SIZE / 100))){
                updateProgressBar(sNthFrame/(Vital.BATCH_SIZE * Vital.FRAME_WINDOW_SIZE / 100));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        readyForNextImage();
        if(isFinishAnalysis){
            Intent intent = new Intent(getContext(), ResultActivity.class);
            getContext().startActivity(intent);
//            if(FLAG_INNER_TEST){
//                mFinishPopup.show();
//            }else{
//
//            }
        }
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try{
            mFrontCameraExecutor.shutdown();
            mFrontCameraExecutor.awaitTermination(
                    Long.MAX_VALUE,
                    TimeUnit.NANOSECONDS
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResults(MPImage input, Bitmap original, EnhanceFaceDetector.ResultBundle resultBundle) {
        lastFrameUtcTimeMs = System.currentTimeMillis();
        mOriginalBitmap = original;
        if(sNthFrame <= 600){
            processImage(input, resultBundle);
        }
    }

    private void stopPrediction(String type){
        if(type.equals(Config.TYPE_OF_BIG)){
            mGuidePopupText.setText(R.string.face_big_detection);
        }else if(type.equals(Config.TYPE_OF_SMALL)){
            mGuidePopupText.setText(R.string.face_no_detection);
        }else if(type.equals(Config.TYPE_OF_OUT)){
            mGuidePopupText.setText(R.string.face_out_detection);
        }
        mTrackingOverlayView.clear();
        sNthFrame = 0;
        mBpmAnalysisViewModel.clearAnalysis();
        updateProgressBar(mProgressBar.getMin());
        mGuidePopupView.show();
        mGreenChart.clearValues();
        isStopPredict = true;
    }

    @Override
    public void onError(String error, int errorCode) {

    }

    public long getLastFrameUtcTimeMs(){
        return lastFrameUtcTimeMs;
    }

    private void updateProgressBar(int progress){
        if((progress == mProgressBar.getMin())
                && (mProgressBar.getProgress() == mProgressBar.getMin())) return;
        mProgressBar.setProgress(progress);
        mProgressBar.invalidate();
    }

    private void updateVitalSignValue(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                hrValueView.setText(String.valueOf(ResultVitalSign.vitalSignData.HR_result));
                rrValueView.setText(String.valueOf(ResultVitalSign.vitalSignData.RR_result));
                sdnnValueView.setText(String.valueOf(ResultVitalSign.vitalSignData.sdnn_result));
                stressValueView.setText(String.valueOf(ResultVitalSign.vitalSignData.LF_HF_ratio));
                spo2ValueView.setText(String.valueOf(ResultVitalSign.vitalSignData.spo2_result));
                sbpValueView.setText(String.valueOf(ResultVitalSign.vitalSignData.SBP));
                dbpValueView.setText(String.valueOf(ResultVitalSign.vitalSignData.DBP));
            }
        });
    }
}