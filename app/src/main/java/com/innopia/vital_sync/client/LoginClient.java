package com.innopia.vital_sync.client;

import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.service.LoginRequest;
import com.innopia.vital_sync.service.LoginResponse;
import com.innopia.vital_sync.service.LoginService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginClient {
    private static LoginClient instance;
    private static Retrofit retrofit;

    public static LoginClient getInstance(){
        if (instance == null) {
            instance = new LoginClient();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.interceptors().add(interceptor);

            retrofit = new Retrofit.Builder()
                    .baseUrl(Config.LOCAL_SERVER_ADDRESS) // 여기에 서버의 base URL 입력
                    .client(builder.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return instance;
    }

    public void login(LoginRequest request, final LoginResponseListener listener) {
        LoginService service = retrofit.create(LoginService.class);
        Call<LoginResponse> call = service.postLogin(request);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    // 서버에서 성공적으로 응답을 받았을 때
                    LoginResponse loginResponse = response.body();
                    listener.onSuccess(loginResponse);
                } else {
                    // 서버에서 오류 응답을 받았을 때
                    listener.onError("로그인 실패");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // 통신 실패시
                listener.onError("통신 실패: " + t.getMessage());
            }
        });
    }

    public interface LoginResponseListener {
        void onSuccess(LoginResponse response);
        void onError(String message);
    }

    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
}
