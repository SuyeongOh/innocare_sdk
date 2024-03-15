package com.innopia.vital_sync.client;

import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.service.GtRequest;
import com.innopia.vital_sync.service.GtResponse;
import com.innopia.vital_sync.service.GtService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GtClient {
    private static GtClient instance;
    private static Retrofit retrofit;

    public static GtClient getInstance(){
        if (instance == null) {
            instance = new GtClient();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.interceptors().add(interceptor);

            if(Config.LOCAL_SERVER_ADDRESS.equals("")){
                retrofit = new Retrofit.Builder()
                        .baseUrl(Config.CLOUD_SERVER_ADDRESS + Config.SERVER_PORT_HEADER
                                + Config.SERVER_VITAL_PORT + Config.SERVER_PORT_FOOTER) // 여기에 서버의 base URL 입력
                        .client(builder.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            } else{
                retrofit = new Retrofit.Builder()
                        .baseUrl(Config.LOCAL_SERVER_ADDRESS) // 여기에 서버의 base URL 입력
                        .client(builder.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }

        }

        return instance;
    }

    public void postGT(GtRequest request, final GtClient.GtResponseListener listener) {
        GtService service = retrofit.create(GtService.class);
        Call<GtResponse> call = service.postGT(request);

        call.enqueue(new Callback<GtResponse>() {
            @Override
            public void onResponse(Call<GtResponse> call, Response<GtResponse> response) {
                if (response.isSuccessful()) {
                    // 서버에서 성공적으로 응답을 받았을 때
                    GtResponse gtResponse = response.body();
                    listener.onSuccess(gtResponse);
                } else {
                    // 서버에서 오류 응답을 받았을 때
                    listener.onError("로그인 실패");
                }
            }

            @Override
            public void onFailure(Call<GtResponse> call, Throwable t) {
                // 통신 실패시
                listener.onError("통신 실패: " + t.getMessage());
            }
        });
    }

    public interface GtResponseListener {
        void onSuccess(GtResponse response);
        void onError(String message);
    }

    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
}
