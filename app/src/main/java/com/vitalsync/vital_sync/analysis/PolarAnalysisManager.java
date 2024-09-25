package com.vitalsync.vital_sync.analysis;

import android.content.Context;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.PolarDeviceInfo;
import com.polar.sdk.api.model.PolarEcgData;
import com.polar.sdk.api.model.PolarHrData;
import com.polar.sdk.api.model.PolarPpgData;
import com.polar.sdk.api.model.PolarPpiData;
import com.polar.sdk.api.model.PolarSensorSetting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;

public class PolarAnalysisManager {
    public static final String TAG = "PolarAnalysisManager";
    public static PolarAnalysisManager sH10Instance;
    public static PolarAnalysisManager sVerityInstance;
    private Context _context;
    private String deviceId;
    private PolarBleApi polarApi;

    private Disposable ecgDisposable;
    private Disposable hrDisposable;
    private Disposable ppgDisposable;
    private Disposable ppiDisposable;

    private boolean isInitialized = false;
    private HandlerThread ecgThread = new HandlerThread("ecgThread");
    private HandlerThread hrThread = new HandlerThread("hrThread");
    private HandlerThread ppgThread = new HandlerThread("ppgThread");
    private HandlerThread ppiThread = new HandlerThread("ppiThread");

    private DeviceStatusListener statusListener;
    private DataResponseListener dataResponseListener;

    private String deviceType = "";

    public static PolarAnalysisManager getH10Instance() {
        if (sH10Instance == null) {
            sH10Instance = new PolarAnalysisManager();
        }
        return sH10Instance;
    }

    public static PolarAnalysisManager getVerityInstance() {
        if (sVerityInstance == null) {
            sVerityInstance = new PolarAnalysisManager();
        }
        return sVerityInstance;
    }

    public void init(Context context, String id) {
        _context = context;
        deviceId = id;
        if(!isInitialized){
            isInitialized = true;
            initInternal();
        }
    }

    public void initInternal() {
        Set<PolarBleApi.PolarBleSdkFeature> polarSet = new HashSet<>();
        polarSet.add(PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING);
        polarSet.add(PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO);
        polarSet.add(PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO);
        polarSet.add(PolarBleApi.PolarBleSdkFeature.FEATURE_HR);
        polarApi = PolarBleApiDefaultImpl.defaultImplementation(_context, polarSet);
        polarApi.setApiCallback(polarApiCallback);

        ecgThread.start();
        hrThread.start();
        ppgThread.start();
        ppiThread.start();
    }

    public void connect(){
        polarApi.setAutomaticReconnection(true);
        try {
            polarApi.connectToDevice(deviceId);
            statusListener.onConnecting();
        } catch (PolarInvalidArgument e){
            e.printStackTrace();
            statusListener.onError();
        }

    }

    public void startStream(){
        streamHR();
        if(deviceType.contains("H10")){
            streamECG();
            return;
        }
        streamPPG();
        streamPPI();

    }

    public void stopStream(){
        if(ecgDisposable != null) ecgDisposable.dispose();
        if(hrDisposable != null) hrDisposable.dispose();
        if(ppiDisposable != null) ppiDisposable.dispose();
        if(ppgDisposable != null) ppgDisposable.dispose();
    }
    public void destroy(){
        try {
            polarApi.disconnectFromDevice(deviceId);
        } catch (PolarInvalidArgument e){
            polarApi.cleanup();
            e.printStackTrace();
        }

        ecgThread.quitSafely();
        hrThread.quitSafely();
        ppgThread.quitSafely();
        ppiThread.quitSafely();

        ecgDisposable.dispose();
        hrDisposable.dispose();
        ppiDisposable.dispose();
        ppgDisposable.dispose();

        ecgDisposable = null;
        hrDisposable = null;
        ppiDisposable = null;
        ppgDisposable = null;
    }

    private void streamECG() {
        boolean isDisposed = ecgDisposable == null || ecgDisposable.isDisposed();
        if (isDisposed) {
            ecgDisposable = polarApi.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ECG)
                    .toFlowable()
                    .flatMap(polarSensorSetting -> polarApi.startEcgStreaming(deviceId, polarSensorSetting))
                    .observeOn(AndroidSchedulers.from(ecgThread.getLooper()))
                    .subscribe(polarEcgData -> {
                                if(dataResponseListener != null) dataResponseListener.EcgDataReceived(polarEcgData);
                                Log.d(TAG, "ecg update");
                            },
                            throwable -> {
                                Log.e(TAG, "Ecg Stream failed !!");
                                throwable.printStackTrace();
                            });
        } else {
            // NOTE stops streaming if it is "running"
            ecgDisposable.dispose();
            ecgDisposable = null;
        }
    }

    private void streamHR() {
        boolean isDisposed = hrDisposable == null || hrDisposable.isDisposed();
        PolarBleApi.PolarDeviceDataType deviceDataType = PolarBleApi.PolarDeviceDataType.PPG;
        if(deviceType.contains("H10")){
            deviceDataType = PolarBleApi.PolarDeviceDataType.ECG;
        }

        if (isDisposed) {
            hrDisposable = polarApi.startHrStreaming(deviceId)
                    .observeOn(AndroidSchedulers.from(hrThread.getLooper()))
                    .subscribe(polarHrData -> {
                                if(dataResponseListener != null) dataResponseListener.HrDataReceived(polarHrData);
                                Log.d(TAG, "hr update");
                            },
                            throwable -> {
                                Log.e(TAG, "Hr Stream failed !!");
                                throwable.printStackTrace();
                            });
        } else {
            // NOTE stops streaming if it is "running"
            hrDisposable.dispose();
            hrDisposable = null;
        }
    }

    private void streamPPI() {
        boolean isDisposed = ppiDisposable == null || ppiDisposable.isDisposed();
        if (isDisposed) {
            ppiDisposable = polarApi.startPpiStreaming(deviceId)
                    .observeOn(AndroidSchedulers.from(ppiThread.getLooper()))
                    .subscribe(polarPpiData -> {
                                dataResponseListener.PpiDataReceived(polarPpiData);
                                Log.d(TAG, "ppi update");
                            },
                            throwable -> {
                                Log.e(TAG, "Ppi Stream failed !!");
                                throwable.printStackTrace();
                                ppiDisposable.dispose();
                            });
        } else {
            // NOTE stops streaming if it is "running"
            ppiDisposable.dispose();
            ppiDisposable = null;
        }
    }

    private void streamPPG() {
        boolean isDisposed = ppgDisposable == null || ppgDisposable.isDisposed();
        if (isDisposed) {
            //verity sampling_rate = 55Hz
            ppgDisposable = polarApi.requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.PPG)
                    .toFlowable()
                    .flatMap(polarSensorSetting ->
                            polarApi.startPpgStreaming(deviceId, polarSensorSetting.maxSettings()))
                    .observeOn(AndroidSchedulers.from(ppgThread.getLooper()))
                    .subscribe(polarPpgData -> {
                                dataResponseListener.PpgDataReceived(polarPpgData);
                                Log.d(TAG, "ppg update");
                            },
                            throwable -> {
                                Log.e(TAG, "Ppg Stream failed !!");
                                throwable.printStackTrace();
                                ppgDisposable.dispose();
                            });
        } else {
            // NOTE stops streaming if it is "running"
            ppgDisposable.dispose();
            ppgDisposable = null;
        }
    }

    public String getDeviceId(){
        return deviceId;
    }
    public void setDeviceStatusListener(DeviceStatusListener listener){
        statusListener = listener;
    }

    public void setDataResponseListener(DataResponseListener listener){
        dataResponseListener = listener;
    }

    private PolarBleApiCallback polarApiCallback = new PolarBleApiCallback() {
        @Override
        public void batteryLevelReceived(@NonNull String identifier, int level) {
            // level: 0~100
            if (level < 10) {
                Toast.makeText(_context, "Battery is Low :: $level%", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void blePowerStateChanged(boolean powered) {
            super.blePowerStateChanged(powered);
        }

        @Override
        public void bleSdkFeatureReady(@NonNull String identifier, @NonNull PolarBleApi.PolarBleSdkFeature feature) {
            Log.d("Polar", "Feature Ready");
//            //H10은 지원안함
//            streamPPG();
//            streamPPI();
        }

        @Override
        public void deviceConnected(@NonNull PolarDeviceInfo polarDeviceInfo) {
            Toast.makeText(_context, "Connect to Device : " + deviceId, Toast.LENGTH_SHORT).show();
            if(polarDeviceInfo.getName().contains("H10")){
                deviceType = "H10";
            } else if(polarDeviceInfo.getName().contains("Sense")){
                deviceType = "Verity";
            } else{
                deviceId = "";
            }
            statusListener.onConnected();
        }

        @Override
        public void deviceConnecting(@NonNull PolarDeviceInfo polarDeviceInfo) {
            Toast.makeText(_context, "Connecting to Device : " + deviceId, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void deviceDisconnected(@NonNull PolarDeviceInfo polarDeviceInfo) {
            Toast.makeText(_context, "Disconnect to Device : " + deviceId, Toast.LENGTH_SHORT).show();
            statusListener.onDisconnect();
        }

        @Override
        public void disInformationReceived(@NonNull String identifier, @NonNull UUID uuid, @NonNull String value) {
            if (uuid == UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")) {
                Toast.makeText(_context, "Device info :: (FW ver - $identifier  ${value.trim()})", Toast.LENGTH_SHORT).show();
            }
        }
    };


    public interface DeviceStatusListener{
        void onConnected();
        void onDisconnect();
        void onConnecting();
        void onError();
    }

    public interface DataResponseListener{
        void EcgDataReceived(PolarEcgData ecgData);
        void HrDataReceived(PolarHrData hrData);
        void PpgDataReceived(PolarPpgData ppgData);
        void PpiDataReceived(PolarPpiData ppiData);
    }
}
