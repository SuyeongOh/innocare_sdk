package com.inniopia.funnylabs_sdk;

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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedKeypoint;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;
import com.inniopia.funnylabs_sdk.data.Constant;
import com.inniopia.funnylabs_sdk.data.ResultVitalSign;
import com.inniopia.funnylabs_sdk.ui.CommonPopupView;
import com.robinhood.ticker.TickerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

public class MainFragment extends Fragment implements EnhanceFaceDetector.DetectorListener {


    //View Variable
    private PreviewView mCameraView;
    private OverlayView mTrackingOverlayView;
    private ProgressBar mProgressBar;
    private BpmAnalysisViewModel mBpmAnalysisViewModel;
    private CommonPopupView mGuidePopupView;
    private AlertDialog mFinishPopup;
    private TextView mGuidePopupText;
    private LineChart mHrChart;
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
    private Button reStartBtn;
    private Button nextPageBtn;

    private View vitalValueLayout;
    private LineDataSet mHrDataset;
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

    HandlerThread thread_g = new HandlerThread("G signal Thread");
    HandlerThread thread_hr = new HandlerThread("hr signal Thread");
    HandlerThread thread_bvp = new HandlerThread("bvp signal Thread");

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

        thread_g.start();
        thread_bvp.start();
        thread_hr.start();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Face Detection
        mCameraView = view.findViewById(R.id.view_finder);
        mTrackingOverlayView = view.findViewById(R.id.tracking_overlay);
        mProgressBar = view.findViewById(R.id.progress);


        if(Config.FLAG_INNER_TEST){
            mVitalGroup = view.findViewById(R.id.vital_info_group);
            mVitalGroup.setVisibility(View.VISIBLE);
            mBvpChart = view.findViewById(R.id.bvp_chart);
            mGreenChart = view.findViewById(R.id.green_chart);
            mHrChart = view.findViewById(R.id.hr_chart);
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
        reStartBtn = view.findViewById(R.id.vital_recheck_btn);
        nextPageBtn = view.findViewById(R.id.vital_next_page);

        reStartBtn.setVisibility(View.INVISIBLE);
        nextPageBtn.setVisibility(View.INVISIBLE);
        initListener();
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
                        stopPrediction(Constant.TYPE_OF_OUT);
                    }
                    readyForNextImage();
                    return;
                } else if(mTrackingOverlayView.isBigSize(box)){
                    if(!isStopPredict) {
                        stopPrediction(Constant.TYPE_OF_BIG);
                    }
                    readyForNextImage();
                    return;
                } else if (mTrackingOverlayView.isSmallSize(box)) {
                    if(!isStopPredict) {
                        stopPrediction(Constant.TYPE_OF_SMALL);
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
                Bitmap croppedFaceBitmap = Bitmap.createBitmap(mOriginalBitmap, rect.left, rect.top, rect.width(), rect.height());
                faceImageModel = new FaceImageModel(croppedFaceBitmap, getLastFrameUtcTimeMs());
                Log.d("Result", "Nth Frame : " + sNthFrame);
                sNthFrame ++;
                isFinishAnalysis = mBpmAnalysisViewModel.addFaceImageModel(faceImageModel);
                if(Config.FLAG_INNER_TEST){
                    Vital vital = mBpmAnalysisViewModel.getVital();
                    VitalLagacy lagacy = vital.getVitalLagacy();



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
                            mGreenData.setColor(Color.GREEN);
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
                        new Handler(thread_hr.getLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                List<Entry> entryList = new ArrayList<>();
                                double[] bvp_signal = lagacy.getHrSignal();
                                float filter_interval = VitalLagacy.VIDEO_FRAME_RATE / (float)VitalLagacy.BUFFER_SIZE;
                                for(int i = 0; i < bvp_signal.length; i++){
                                    entryList.add(new Entry((float)(i + 0.83/filter_interval) * filter_interval * 60, (float)bvp_signal[i]));
                                }
                                mHrDataset = new LineDataSet(entryList, "");
                                mHrDataset.setDrawCircles(false);
                                mHrDataset.setColor(Color.CYAN);
                                LineData data = new LineData(mHrDataset);
                                mHrChart.setData(data);
                                mHrChart.notifyDataSetChanged();
                                mHrChart.invalidate();
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
            if(Config.FLAG_INNER_TEST){
                new Handler(Looper.getMainLooper()).post(() -> mFinishPopup.show());
            }else{
                Intent intent = new Intent(getContext(), ResultActivity.class);
                getContext().startActivity(intent);
            }
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
            thread_g.quitSafely();
            thread_bvp.quitSafely();
            thread_hr.quitSafely();
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
        switch (type) {
            case Constant.TYPE_OF_BIG:
                mGuidePopupText.setText(R.string.face_big_detection);
                mGuidePopupView.show();
                break;
            case Constant.TYPE_OF_SMALL:
                mGuidePopupText.setText(R.string.face_no_detection);
                mGuidePopupView.show();
                break;
            case Constant.TYPE_OF_OUT:
                mGuidePopupText.setText(R.string.face_out_detection);
                mGuidePopupView.show();
                break;
        }
        mTrackingOverlayView.clear();
        sNthFrame = 0;
        mBpmAnalysisViewModel.clearAnalysis();
        updateProgressBar(mProgressBar.getMin());
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
        Log.d("jupiter", "current progress bar : " + mProgressBar.getProgress());
        Log.d("jupiter", "update progress bar : " + progress);
        if((progress == mProgressBar.getMin())
                && (mProgressBar.getProgress() == mProgressBar.getMin()))
            return;
        mProgressBar.setProgress(progress);
        mProgressBar.invalidate();
    }

    private void updateVitalSignValue(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                hrValueView.setText(String.valueOf(Math.round(ResultVitalSign.vitalSignData.HR_result)));
                rrValueView.setText(String.valueOf(Math.round(ResultVitalSign.vitalSignData.RR_result)));
                sdnnValueView.setText(String.valueOf(Math.round(ResultVitalSign.vitalSignData.sdnn_result)));
                stressValueView.setText(String.valueOf(Math.round(ResultVitalSign.vitalSignData.LF_HF_ratio)));
                spo2ValueView.setText(String.valueOf(Math.round(ResultVitalSign.vitalSignData.spo2_result)));
                sbpValueView.setText(String.valueOf(Math.round(ResultVitalSign.vitalSignData.SBP)));
                dbpValueView.setText(String.valueOf(Math.round(ResultVitalSign.vitalSignData.DBP)));
            }
        });
    }

    private void initChart(){
        //BVP chart
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
        legend.setYEntrySpace(3);

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
        yAxisLeft.resetAxisMinimum();
        yAxisLeft.setAxisLineWidth(2);

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxis = mBvpChart.getAxisRight();
        yAxis.setDrawLabels(false); // label 삭제
        yAxis.setTextColor(Color.BLACK);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisLineWidth(2);
        yAxisLeft.resetAxisMinimum();
        yAxis.setAxisMaximum((float) 1); // 최댓값
        yAxis.setGranularity((float) 0.1);

        //G signal Chart
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

        //HR chart
        mHrChart.getDescription().setText("HR");
        mHrChart.getDescription().setEnabled(true);
        Legend legend_hr = mHrChart.getLegend();
        legend_hr.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend_hr.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend_hr.setForm(Legend.LegendForm.CIRCLE);
        legend_hr.setFormSize(10);
        legend_hr.setTextSize(13);
        legend_hr.setTextColor(Color.parseColor("#A3A3A3"));
        legend_hr.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend_hr.setDrawInside(true);
        legend_hr.setYEntrySpace(3);

        // XAxis (아래쪽) - 선 유무, 사이즈, 색상, 축 위치 설정
        XAxis xAxis_hr = mHrChart.getXAxis();
        xAxis_hr.setDrawAxisLine(false);
        xAxis_hr.setDrawGridLines(false);
        xAxis_hr.setPosition(XAxis.XAxisPosition.BOTTOM); // x축 데이터 표시 위치
        xAxis_hr.setGranularity(1f);
        xAxis_hr.setTextSize(14f);
        xAxis_hr.setTextColor(Color.BLACK);
        xAxis_hr.setSpaceMin(0.1f); // Chart 맨 왼쪽 간격 띄우기
        xAxis_hr.setSpaceMax(0.1f); // Chart 맨 오른쪽 간격 띄우기

        // YAxis(Right) (왼쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxisLeft_hr = mHrChart.getAxisLeft();
        yAxisLeft_hr.setDrawAxisLine(false);

        // YAxis(Left) (오른쪽) - 선 유무, 데이터 최솟값/최댓값, 색상
        YAxis yAxis_hr = mHrChart.getAxisRight();
        yAxis_hr.setDrawLabels(false); // label 삭제
        yAxis_hr.setTextColor(Color.BLACK);
        yAxis_hr.setDrawAxisLine(false);
        yAxis_hr.setAxisLineWidth(2);
        yAxis_hr.setAxisMinimum(0f); // 최솟값
        yAxis_hr.setAxisMaximum((float) 512); // 최댓값
        yAxis_hr.setGranularity((float) 512);
    }

    private void initListener(){
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
                        reStartBtn.setVisibility(View.VISIBLE);
                        nextPageBtn.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                }).create();

        reStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStopPredict = false;
                sNthFrame = 0;
                ResultVitalSign.vitalSignData.init();
                mBpmAnalysisViewModel = new BpmAnalysisViewModel(new Application(), requireContext());
            }
        });
        nextPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ResultActivity.class);
                getContext().startActivity(intent);
            }
        });
    }
}