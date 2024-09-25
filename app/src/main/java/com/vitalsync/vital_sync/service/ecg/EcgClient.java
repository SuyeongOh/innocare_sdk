package com.vitalsync.vital_sync.service.ecg;

import android.util.Log;

import com.polar.sdk.api.model.PolarEcgData;
import com.polar.sdk.api.model.PolarPpgData;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.service.login.LoginClient;
import com.vitalsync.vital_sync.service.vital.VitalService;

import java.util.ArrayList;

import jsat.utils.ArrayUtils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EcgClient {
    private final String TAG = "Polar";
    private static EcgClient sInstance;
    private static Retrofit retrofit;

    public static EcgClient getInstance(){
        if (sInstance == null) {
            sInstance = new EcgClient();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.interceptors().add(interceptor);

            if(Config.LOCAL_SERVER_ADDRESS.equals("")){
                retrofit = new Retrofit.Builder()
                        .baseUrl(Config.CLOUD_SERVER_ADDRESS + Config.SERVER_PORT_HEADER
                                + Config.SERVER_VITAL_PORT + Config.SERVER_PORT_FOOTER)
                        .client(builder.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } else{
                retrofit = new Retrofit.Builder()
                        .baseUrl(Config.LOCAL_SERVER_ADDRESS)
                        .client(builder.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
        }

        return sInstance;
    }

    public void requestPolar(ArrayList<PolarEcgData.PolarEcgDataSample> ecg_list
            , ArrayList<PolarPpgData.PolarPpgSample> ppg_list
            , long measureTime){

        EcgRequest request = new EcgRequest(measureTime, Config.USER_ID);

        ArrayList<Integer> ecg_data = new ArrayList<>();

        if(ecg_list.isEmpty() && ppg_list.isEmpty()){
            Log.d(TAG, "No ECG/PPG Signal !!");
        } else{
            if(!ecg_list.isEmpty()){
                for(PolarEcgData.PolarEcgDataSample sample : ecg_list){
                    ecg_data.add(sample.getVoltage());
                }
                request.setEcgSignal(ecg_data.stream().mapToInt(Integer::intValue).toArray());
            } else{
                Log.d(TAG, "No ECG Signal !!");
            }

            ArrayList<Integer> ppg_data = new ArrayList<>();
            if(!ppg_list.isEmpty()){
                for(PolarPpgData.PolarPpgSample sample : ppg_list){
                    //sample 0:green, 1:red, 3:적외선
                    int g_sample = sample.getChannelSamples().get(0);
                    ppg_data.add(g_sample);
                }
                request.setPpgsignal(ppg_data.stream().mapToInt(Integer::intValue).toArray());
            } else{
                Log.d(TAG, "No PPG Signal !!");
            }
            try {
                retrofit.create(VitalService.class)
                        .postVitalPolar(request)
                        .enqueue(new Callback<EcgResponse>() {
                            @Override
                            public void onResponse(Call<EcgResponse> call, Response<EcgResponse> response) {
                                Log.d("Polar", response.message());
                            }

                            @Override
                            public void onFailure(Call<EcgResponse> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
}
