package com.vitalsync.vital_sync.service.ecg;

import com.polar.sdk.api.model.PolarEcgData;
import com.polar.sdk.api.model.PolarPpgData;
import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.service.login.LoginClient;
import com.vitalsync.vital_sync.service.vital.VitalService;

import java.util.ArrayList;

import jsat.utils.ArrayUtils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EcgClient {
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
                                + Config.SERVER_LOGIN_PORT + Config.SERVER_PORT_FOOTER)
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

    public Response<EcgResponse> requestPolar(ArrayList<PolarEcgData.PolarEcgDataSample> ecg_list
            , ArrayList<PolarPpgData.PolarPpgSample> ppg_list
            , long measureTime){

        Response<EcgResponse> response = null;

        EcgRequest request = new EcgRequest(measureTime);

        ArrayList<Integer> ecg_data = new ArrayList<>();
        if(!ecg_list.isEmpty()){
            for(PolarEcgData.PolarEcgDataSample sample : ecg_list){
                ecg_data.add(sample.getVoltage());
            }
            request.setEcgSignal(ecg_data.stream().mapToInt(Integer::intValue).toArray());
        }

        ArrayList<Integer> ppg_data = new ArrayList<>();
        if(!ppg_list.isEmpty()){
            for(PolarPpgData.PolarPpgSample sample : ppg_list){
                //sample 0:green, 1:red, 3:적외선
                int g_sample = sample.getChannelSamples().get(0);
                ppg_data.add(g_sample);
            }
            request.setPpgsignal(ppg_data.stream().mapToInt(Integer::intValue).toArray());
        }
        try {
            response = retrofit.create(VitalService.class)
                    .postVitalVerity(request)
                    .execute();
        }catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }


    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
}
