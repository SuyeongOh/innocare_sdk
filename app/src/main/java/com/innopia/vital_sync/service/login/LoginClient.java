package com.innopia.vital_sync.service.login;

import com.innopia.vital_sync.data.Config;

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

    public void login(LoginRequest request, final LoginResponseListener listener) {
        LoginService service = retrofit.create(LoginService.class);
        Call<LoginResponse> call = service.Login(request.id);

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
