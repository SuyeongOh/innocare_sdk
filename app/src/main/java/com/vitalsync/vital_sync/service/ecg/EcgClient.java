package com.vitalsync.vital_sync.service.ecg;

import com.vitalsync.vital_sync.data.Config;
import com.vitalsync.vital_sync.service.login.LoginClient;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EcgClient {
    private static EcgClient sInstance;
    private static Retrofit retrofit;

    public EcgClient getInstance(){
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



    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
}
