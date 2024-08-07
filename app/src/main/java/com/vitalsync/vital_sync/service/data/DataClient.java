package com.vitalsync.vital_sync.service.data;

import com.vitalsync.vital_sync.data.Config;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DataClient {
    public static DataClient instance;
    private static Retrofit retrofit;

    public static DataClient getInstance(){
        if (instance == null) {
            instance = new DataClient();
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

        return instance;
    }

    public void requestData(Callback<List<DataResponse>> callback){
        DataService service = retrofit.create(DataService.class);
        Call<List<DataResponse>> callData = service.getMeasuredData(Config.USER_ID);

        callData.enqueue(callback);
    }
    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

}
