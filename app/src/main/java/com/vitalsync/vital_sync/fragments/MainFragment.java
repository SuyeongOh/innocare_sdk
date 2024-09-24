package com.vitalsync.vital_sync.fragments;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYPlot;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult;
import com.polar.sdk.api.model.PolarEcgData;
import com.polar.sdk.api.model.PolarHrData;
import com.polar.sdk.api.model.PolarPpgData;
import com.polar.sdk.api.model.PolarPpiData;
import com.robinhood.ticker.TickerView;
import com.vitalsync.vital_sync.R;
import com.vitalsync.vital_sync.activities.MainActivity;
import com.vitalsync.vital_sync.activities.ResultActivity;
import com.vitalsync.vital_sync.analysis.BpmAnalysisViewModel;
import com.vitalsync.vital_sync.analysis.EnhanceFaceDetector;
import com.vitalsync.vital_sync.analysis.FaceImageModel;
import com.vitalsync.vital_sync.analysis.PolarAnalysisManager;
import com.vitalsync.vital_sync.camera.AutoFitSurfaceView;
import com.vitalsync.vital_sync.camera.CameraSizes;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.data.Constant;
import com.vitalsync.vital_sync.data.ResultVitalSign;
import com.vitalsync.vital_sync.service.ecg.EcgClient;
import com.vitalsync.vital_sync.ui.CommonPopupView;
import com.vitalsync.vital_sync.ui.CustomCountdownView;
import com.vitalsync.vital_sync.ui.EcgPlotter;
import com.vitalsync.vital_sync.ui.OverlayView;
import com.vitalsync.vital_sync.utils.ImageUtils;
import com.vitalsync.vital_sync.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

public class MainFragment extends Fragment implements EnhanceFaceDetector.DetectorListener {

    //Camera2 variable

    private CameraCharacteristics characteristics;
    private CameraManager cameraManager;
    private ImageReader imageReader;
    private HandlerThread cameraThread;
    private HandlerThread imageReaderThread;
    private HandlerThread thread_preview;
    private CameraDevice Camera;
    private String cameraId;
    private CameraCaptureSession cameraCaptureSession;
    private AutoFitSurfaceView autoFitSurfaceView;
    private ImageView imageSurfaceView;
    private Handler cameraHandler;
    private Handler imageReaderHandler;
    private final HandlerThread plotThread = new HandlerThread("plot");

    //View Variable
    private OverlayView mTrackingOverlayView;
    private ProgressBar mProgressBar;
    private BpmAnalysisViewModel mBpmAnalysisViewModel;
    private CommonPopupView mGuidePopupView;
    private AlertDialog mFinishPopup;
    private CustomCountdownView mCountDownView;
    private TextView mGuidePopupText;
    private View mVitalGroup;
    private CountDownTimer mCalibrationTimer;
    private TickerView hrValueView;
    private TickerView rrValueView;
    private TickerView sdnnValueView;
    private TickerView spo2ValueView;
    private TickerView stressValueView;
    private TickerView sbpValueView;
    private TickerView dbpValueView;
    private ImageView homeButton;
    private Button reStartBtn;
    private Button nextPageBtn;
    private View vitalValueLayout;

    //Camera Property variable
    private EnhanceFaceDetector faceDetector;
    private ExecutorService mFrontCameraExecutor;
    public Bitmap mOriginalBitmap;

    private int sNthFrame = 0;

    private final Rect faceROI = new Rect();
    private boolean isFinishAnalysis = false;
    private boolean isFixedFace = false;
    private boolean isStopPredict = false;
    private boolean calibrationFinish = false;
    private boolean calibrationTimerStart = false;
    private boolean isTablet = false;
    private long faceModelTime;
    private long startTime;
    private long startTime_2000_1_1 = 0;
    private final Range<Integer> fpsRange = new Range<>(25, Config.TARGET_FRAME);
    private final ArrayList<PolarEcgData.PolarEcgDataSample> polarEcgData = new ArrayList<>();
    private final ArrayList<PolarHrData.PolarHrSample> polarRriData = new ArrayList<>();

    private final ArrayList<PolarPpgData.PolarPpgSample> polarPpgData = new ArrayList<>();
    private final ArrayList<PolarPpiData.PolarPpiSample> polarPpiData = new ArrayList<>();
    private PolarAnalysisManager polarH10Manager;
    private PolarAnalysisManager polarVerityManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBpmAnalysisViewModel = new BpmAnalysisViewModel(new Application(), requireContext());

        mFrontCameraExecutor = Executors.newSingleThreadExecutor();

        //밝기 100%로 올리기
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.screenBrightness = 1.0f;
        getActivity().getWindow().setAttributes(layoutParams);

        faceDetector = new EnhanceFaceDetector(requireContext(), this);
        faceDetector.setupFaceDetector();

        polarH10Manager = PolarAnalysisManager.getH10Instance();
        polarVerityManager = PolarAnalysisManager.getVerityInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        FrameLayout cameraContainer = view.findViewById(R.id.container_surface);
        View surfaceView = LayoutInflater.from(requireContext()).inflate(
                R.layout.layout_surface_container, cameraContainer, false);
        cameraContainer.addView(surfaceView);

        mCountDownView = view.findViewById(R.id.countdown);

        initThread();

        //Camera2
        autoFitSurfaceView = view.findViewById(R.id.view_finder_camera2);
        imageSurfaceView = view.findViewById(R.id.view_finder_image);
        mTrackingOverlayView = view.findViewById(R.id.tracking_overlay);
        mProgressBar = view.findViewById(R.id.progress);

        if (Config.FLAG_INNER_TEST) {
            mVitalGroup = view.findViewById(R.id.vital_info_group);
            mVitalGroup.setVisibility(View.VISIBLE);
        }

        //Face Guide Popup
        View viewNoDetectionPopup = inflater.inflate(R.layout.layout_detection_popup, container, false);
        mGuidePopupView = new CommonPopupView(requireContext(), viewNoDetectionPopup);
        mGuidePopupText = viewNoDetectionPopup.findViewById(R.id.text_face_popup);
        mGuidePopupText.setText(R.string.face_no_detection);

        vitalValueLayout = view.findViewById(R.id.vital_info_layout);
        vitalValueLayout.setClickable(false);
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
        initCalibrationTimer();
        initPlot();

        homeButton = view.findViewById(R.id.view_home_button);

        try {
            polarH10Manager.setDataResponseListener(dataResponseListener);
            polarH10Manager.startStream();
            polarVerityManager.setDataResponseListener(dataVerityResponseListener);
            polarVerityManager.startStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.replaceFragment(new LoginFragment());
            }
        });

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cameraId = chooseCamera();
        autoFitSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                openCamera(cameraManager, cameraId, cameraHandler);
                Size[] sizeArray = (characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(Constant.PIXEL_FORMAT));
                Size imageReaderSize = null;
                for (int i = sizeArray.length - 1; i > 0; i--) {
                    if (CameraSizes.isHdRatio(sizeArray[i]) && (sizeArray[i].getWidth() == 640)) {
                        imageReaderSize = sizeArray[i];
                        break;
                    }
                }
                //640x360비율이 없을경우 낮은곳에서 찾기, TODO 640 밑으로 16:9가 없는 카메라 생기면 그때 또 handling
                if (imageReaderSize == null) {
                    for (int i = sizeArray.length - 1; i > 0; i--) {
                        if (CameraSizes.isHdRatio(sizeArray[i]) && (sizeArray[i].getWidth() <= 640)) {
                            imageReaderSize = sizeArray[i];
                            break;
                        }
                    }
                }
                if (imageReaderSize == null) {
                    imageReaderSize = new Size(autoFitSurfaceView.getWidth(), autoFitSurfaceView.getHeight());
                }
                imageReader = ImageReader.newInstance(imageReaderSize.getWidth(), imageReaderSize.getHeight(), Constant.PIXEL_FORMAT, Constant.IMAGE_BUFFER_SIZE);

                Point displaySize = new Point();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    getActivity().getDisplay().getRealSize(displaySize);
                }

                //세로 방향 인지 판단
                int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) { // landscape
                    displaySize.y = displaySize.x * imageReaderSize.getWidth() / imageReaderSize.getHeight();
                } else { // portrait 16:9 >> 9:16 전환
                    displaySize.y = displaySize.x * imageReaderSize.getWidth() / imageReaderSize.getHeight();
                }
                view.post(() -> initCamera());
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            }
        });
    }

    private void initPlot() {
//        ecgPlotter = new EcgPlotter("ECG", 130);
//        ecgPlotter.setListener(ecgPlotListener);
//        //ppgPlotter = new EcgPlotter("PPG", )
//        ecgPlot.addSeries(ecgPlotter.getSeries(), ecgPlotter.getFormatter());
//        ecgPlot.setRangeBoundaries(-1.5, 1.5, BoundaryMode.FIXED);
//        ecgPlot.setRangeStep(StepMode.INCREMENT_BY_FIT, 0.25);
//        ecgPlot.setDomainStep(StepMode.INCREMENT_BY_VAL, 130.0);
//        ecgPlot.setDomainBoundaries(0, 650, BoundaryMode.FIXED);
//        ecgPlot.setLinesPerRangeLabel(2);
    }

    private void initThread() {
        thread_preview = new HandlerThread("preview Thread");
        thread_preview.start();

        cameraThread = new HandlerThread("CameraThread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
        imageReaderThread = new HandlerThread("imageReaderThread");
        imageReaderThread.start();
        imageReaderHandler = new Handler(imageReaderThread.getLooper());
    }

    private void initCamera() {
        createCaptureSession(Camera, Arrays.asList(imageReader.getSurface()), cameraHandler);
    }

    private String chooseCamera() {
        cameraManager = (CameraManager) requireContext().getApplicationContext()
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                characteristics = cameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map != null) {
                    int lens = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (Config.USE_CAMERA_DIRECTION == Constant.CAMERA_DIRECTION_FRONT) {
                        if (lens == CameraCharacteristics.LENS_FACING_FRONT) {
                            return cameraId;
                        }
                    } else {
                        if (lens == CameraCharacteristics.LENS_FACING_BACK) {
                            return cameraId;
                        }
                    }
                }
            }
        } catch (CameraAccessException e) {
            logCameraAccessException(e);
        }
        return null;
    }

    private void openCamera(CameraManager manager, String cameraId, Handler handler) {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Camera = camera;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    refreshFragment();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }

            }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            logCameraAccessException(e);
        }
    }

    private void createCaptureSession(CameraDevice camera, List<Surface> targets, Handler handler) {
        try {
            camera.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        cameraCaptureSession = session;
                        CaptureRequest.Builder requestBuilder = null;
                        requestBuilder = Camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        requestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange);
                        requestBuilder.addTarget(imageReader.getSurface());
                        cameraCaptureSession.setRepeatingRequest(requestBuilder.build(), null, cameraHandler);
                    } catch (CameraAccessException e) {
                        logCameraAccessException(e);
                    }
                    imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            Image inputImage = reader.acquireLatestImage();

                            if (inputImage == null) {
                                return;
                            }
                            if (isFinishAnalysis) {
                                inputImage.close();
                                return;
                            }
                            inputImage.getPlanes();
                            Bitmap bitmapImage = ImageUtils.convertYUV420ToARGB8888(inputImage);
                            if (Config.USE_CAMERA_DIRECTION == Constant.CAMERA_DIRECTION_FRONT) {
                                if (isTablet) {
                                    Matrix flipMatrix = new Matrix();
                                    flipMatrix.setScale(-1, 1);
                                    bitmapImage = Bitmap.createBitmap(
                                            bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), flipMatrix, false);
                                } else {
                                    Matrix rotateMatrix = new Matrix();
                                    Matrix flipMatrix = new Matrix();
                                    rotateMatrix.postRotate(-90);
                                    flipMatrix.setScale(-1, 1);
                                    bitmapImage = Bitmap.createBitmap(
                                            bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), rotateMatrix, false);
                                    bitmapImage = Bitmap.createBitmap(
                                            bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), flipMatrix, false);
                                }
                            }
                            Bitmap finalBitmapImage = bitmapImage;

//                            new Handler(Looper.getMainLooper()).post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    imageSurfaceView.setImageBitmap(Bitmap
//                                            .createScaledBitmap(finalBitmapImage, autoFitSurfaceView.getWidth(), autoFitSurfaceView.getHeight(), false));
//                                }
//                            });
                            autoFitSurfaceView.setBitmap(Bitmap
                                    .createScaledBitmap(finalBitmapImage, autoFitSurfaceView.getWidth(), autoFitSurfaceView.getHeight(), false));
                            if (sNthFrame == 0 && !calibrationTimerStart) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        autoFitSurfaceView.requestLayout();
                                    }
                                });
                                startCalibrationTimer();
                                calibrationTimerStart = true;
                                calibrationFinish = false;
                            }
                            if (!calibrationFinish) {
                                inputImage.close();
                                return;
                            }
                            inputImage.close();

                            if (isFixedFace) {
                                Bitmap faceImage;
                                try {
                                    faceImage = Bitmap.createBitmap(bitmapImage, faceROI.left, faceROI.top, faceROI.width(), faceROI.height());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                                if (sNthFrame == 0) {
                                    startTime = System.currentTimeMillis();
                                    startTime_2000_1_1 = TimeUtils.getCurrentTimeStamp_by_2000_1_1(startTime);
                                }
                                faceModelTime = System.currentTimeMillis();
                                if ((int) ((faceModelTime - startTime) * 100 / (double) 20000)
                                        > sNthFrame / (double) (Config.TARGET_FRAME * Config.ANALYSIS_TIME - 1)) {
                                    updateProgressBar((int) ((faceModelTime - startTime) * 100 / (double) 20000));
                                } else {
                                    updateProgressBar((int) (sNthFrame / (double) (Config.TARGET_FRAME * Config.ANALYSIS_TIME - 1)));
                                }

                                if ((mProgressBar.getMax() == mProgressBar.getProgress()) || sNthFrame == (Config.TARGET_FRAME * Config.ANALYSIS_TIME - 1)) {
                                    isFinishAnalysis = true;
                                }
                                mBpmAnalysisViewModel.addFaceImageModel(new FaceImageModel(faceImage, faceModelTime, isFinishAnalysis));


                                sNthFrame++;
                                if (isFinishAnalysis) {
                                    startTime_2000_1_1 = 0;
                                    EcgClient.getInstance().requestPolar(polarEcgData, polarPpgData, startTime_2000_1_1);
                                    if (Config.FLAG_INNER_TEST) {
                                        updateVitalSignValue();
                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                            @Override
                                            public void run() {
                                                reStartBtn.setVisibility(View.VISIBLE);
                                                if (!Config.USER_ID.equals(getContext().getString(R.string.target_guest))) {
                                                    nextPageBtn.setVisibility(View.VISIBLE);
                                                }

                                                mFinishPopup.show();
                                            }
                                        });
                                    } else {
                                        Intent intent = new Intent(getContext(), ResultActivity.class);
                                        getContext().startActivity(intent);
                                    }
                                }
                            } else {
                                MPImage image = new BitmapImageBuilder(bitmapImage).build();
                                faceDetector.detectAsync(image, bitmapImage, System.currentTimeMillis());
                            }

                        }
                    }, imageReaderHandler);

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, handler);
        } catch (CameraAccessException e) {
            logCameraAccessException(e);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        polarH10Manager.destroy();
        polarVerityManager.destroy();
        cameraHandler.removeCallbacksAndMessages(null);
        imageReaderHandler.removeCallbacksAndMessages(null);
        Camera.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Camera.close();
        cameraThread.quitSafely();
        imageReaderThread.quitSafely();
        thread_preview.quitSafely();
        mCalibrationTimer.cancel();
        mFrontCameraExecutor.shutdown();
        try {
            mFrontCameraExecutor.awaitTermination(
                    Long.MAX_VALUE,
                    TimeUnit.NANOSECONDS
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Runnable postInferenceCallback;

    public void processImage(MPImage image, EnhanceFaceDetector.ResultBundle resultBundle) {
        postInferenceCallback = image::close;
        List<FaceDetectorResult> faceDetectorResults = resultBundle.getResults();
        try {
            if (faceDetectorResults.get(0).detections().size() >= 1) {
                RectF box = faceDetectorResults.get(0).detections().get(0).boundingBox();

                if (mTrackingOverlayView.isOutBoundary(box)) {
                    if (!isStopPredict) {
                        stopPrediction(Constant.TYPE_OF_OUT);
                    }
                    readyForNextImage();
                    return;
                } else if (mTrackingOverlayView.isSmallSize(box)) {
                    if (!isStopPredict) {
                        stopPrediction(Constant.TYPE_OF_SMALL);
                    }
                    readyForNextImage();
                    return;
                }
                isStopPredict = false;
                mGuidePopupView.dismiss();

                isFixedFace = true;
                float width = box.width();
                float height = box.height();
                faceROI.left = (int) (box.left + width / 10);
                faceROI.right = (int) (box.right - width / 10);
                faceROI.top = (int) (box.top + height / 10 * 4);
                faceROI.bottom = (int) (box.bottom - height / 10 * 4);
//                if(Config.LARGE_FACE_MODE){
//                    float width = box.width();
//                    float height = box.height();
//                    box.left -= width/4;
//                    box.right += width/4;
//                    box.top -= height/4;
//                    box.bottom += height/4;
//                    box.round(faceROI);
//                } else if (Config.SMALL_FACE_MODE){
//                    float width = box.width();
//                    float height = box.height();
//                    box.left += width/10;
//                    box.right -= width/10;
//                    box.top += height/10 * 4;
//                    box.bottom -= height/10 * 4;
//                    box.round(faceROI);
//                }else{
//                    box.round(faceROI);
//                }

            }
            if (mTrackingOverlayView != null) {
                FaceDetectorResult result = resultBundle.getResults().get(0);
                mTrackingOverlayView.setResults(result,
                        resultBundle.inputImageWidth,
                        resultBundle.inputImageHeight,
                        Config.USE_CAMERA_DIRECTION == Constant.CAMERA_DIRECTION_FRONT);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        readyForNextImage();
    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    @Override
    public void onResults(MPImage input, Bitmap original, EnhanceFaceDetector.ResultBundle resultBundle) {
        mOriginalBitmap = original;
        processImage(input, resultBundle);
    }

    private void stopPrediction(String type) {
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
        isStopPredict = true;
    }

    @Override
    public void onError(String error, int errorCode) {

    }

    private void updateProgressBar(int progress) {
        if ((progress == mProgressBar.getMin())
                && (mProgressBar.getProgress() == mProgressBar.getMin())) return;
        mProgressBar.setProgress(progress);

        mProgressBar.invalidate();
    }

    private void updateVitalSignValue() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                hrValueView.setText(
                        Config.USER_ID.equals(getContext().getString(R.string.target_guest)) ?
                                String.valueOf(Math.round(ResultVitalSign.vitalSignData.HR)) :
                                Math.round(ResultVitalSign.vitalSignData.HR) + "/" +
                                        Math.round(ResultVitalSign.vitalSignServerData.HR)
                );
                rrValueView.setText(
                        Config.USER_ID.equals(getContext().getString(R.string.target_guest)) ?
                                String.valueOf(Math.round(ResultVitalSign.vitalSignData.RR)) :
                                Math.round(ResultVitalSign.vitalSignData.RR) + "/" +
                                        Math.round(ResultVitalSign.vitalSignServerData.RR)
                );
                sdnnValueView.setText(
                        Config.USER_ID.equals(getContext().getString(R.string.target_guest)) ?
                                String.valueOf(Math.round(ResultVitalSign.vitalSignData.HRV)) :
                                Math.round(ResultVitalSign.vitalSignData.HRV) + "/" +
                                        Math.round(ResultVitalSign.vitalSignServerData.HRV)
                );
                stressValueView.setText(
                        Config.USER_ID.equals(getContext().getString(R.string.target_guest)) ?
                                String.valueOf(Math.round(ResultVitalSign.vitalSignData.STRESS)) :
                                Math.round(ResultVitalSign.vitalSignData.STRESS) + "/" +
                                        Math.round(ResultVitalSign.vitalSignServerData.STRESS)
                );
                spo2ValueView.setText(
                        Config.USER_ID.equals(getContext().getString(R.string.target_guest)) ?
                                String.valueOf(Math.round(ResultVitalSign.vitalSignData.RR)) :
                                Math.round(ResultVitalSign.vitalSignData.SpO2) + "/" +
                                        Math.round(ResultVitalSign.vitalSignServerData.SpO2)
                );
                sbpValueView.setText(
                        Config.USER_ID.equals(getContext().getString(R.string.target_guest)) ?
                                "Sys " + Math.round(ResultVitalSign.vitalSignData.SBP) :
                                "Sys " + Math.round(ResultVitalSign.vitalSignData.SBP) + "/" +
                                        Math.round(ResultVitalSign.vitalSignServerData.SBP)
                );
                dbpValueView.setText(
                        Config.USER_ID.equals(getContext().getString(R.string.target_guest)) ?
                                "Dia " + Math.round(ResultVitalSign.vitalSignData.DBP) :
                                "Dia " + Math.round(ResultVitalSign.vitalSignData.DBP) + "/" +
                                        Math.round(ResultVitalSign.vitalSignServerData.DBP)
                );
            }
        });
    }

    private void initListener() {
        if (Config.USER_ID.equals(getContext().getString(R.string.target_guest))) {
            mFinishPopup = new AlertDialog.Builder(getContext())
                    .setMessage("분석 완료 !!\n")
                    .setNeutralButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
        } else {
            mFinishPopup = new AlertDialog.Builder(getContext())
                    .setTitle("분석 마침")
                    .setMessage("결과화면으로 넘어가시겠습니까?\n")
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
        }


        reStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAnalysis();
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

    private void finishAnalysis() {
        isStopPredict = false;
        sNthFrame = 0;
        ResultVitalSign.vitalSignData.init();
        mBpmAnalysisViewModel = new BpmAnalysisViewModel(new Application(), requireContext());
        reStartBtn.setVisibility(View.INVISIBLE);
        nextPageBtn.setVisibility(View.INVISIBLE);
        mCountDownView.reset();
        mCountDownView.setVisibility(View.VISIBLE);
        mTrackingOverlayView.clear();
        calibrationFinish = false;
        calibrationTimerStart = false;
        isFinishAnalysis = false;
        isFixedFace = false;
        polarEcgData.clear();
        polarRriData.clear();
    }

    private void initCalibrationTimer() {
        mCalibrationTimer = new CountDownTimer(3999, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("vital", "Timer :: " + millisUntilFinished);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mCountDownView.setCountDownText(String.valueOf(millisUntilFinished / 1000));
                    }
                });

            }

            @Override
            public void onFinish() {
                calibrationFinish = true;
                mCountDownView.setVisibility(View.GONE);
            }
        };
    }

    private void startCalibrationTimer() {
        mCalibrationTimer.start();
    }

    private void logCameraAccessException(Exception e) {
        Log.e("Camera", "Can not accessed in Camera : " + e.getMessage());
    }

    private void refreshFragment() {
        getParentFragmentManager().beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }

    private final PolarAnalysisManager.DataResponseListener dataResponseListener = new PolarAnalysisManager.DataResponseListener() {
        @Override
        public void EcgDataReceived(PolarEcgData ecgData) {
            if (startTime_2000_1_1 != 0
                    && (ecgData.getSamples().get(0).getTimeStamp() > startTime_2000_1_1)) {
                polarEcgData.addAll(ecgData.getSamples());
            }
        }

        @Override
        public void HrDataReceived(PolarHrData hrData) {
            if ((startTime_2000_1_1 != 0)) {
                polarRriData.addAll(hrData.getSamples());
            }
        }

        @Override
        public void PpgDataReceived(PolarPpgData ppgData) {

        }

        @Override
        public void PpiDataReceived(PolarPpiData ppiData) {

        }
    };

    private final PolarAnalysisManager.DataResponseListener dataVerityResponseListener = new PolarAnalysisManager.DataResponseListener() {
        @Override
        public void EcgDataReceived(PolarEcgData ecgData) {

        }

        @Override
        public void HrDataReceived(PolarHrData hrData) {

        }

        @Override
        public void PpgDataReceived(PolarPpgData ppgData) {
            if (startTime_2000_1_1 != 0
                    && ppgData.getSamples().get(0).getTimeStamp() > startTime_2000_1_1) {
                polarPpgData.addAll(ppgData.getSamples());
            }
        }

        @Override
        public void PpiDataReceived(PolarPpiData ppiData) {
            if (startTime_2000_1_1 != 0) {
                polarPpiData.addAll(ppiData.getSamples());
            }
        }
    };
}