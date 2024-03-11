package com.innopia.vital_sync.client;

import android.util.Log;

import com.innopia.vital_sync.data.Config;
import com.innopia.vital_sync.service.VitalRequest;
import com.innopia.vital_sync.service.VitalResponse;
import com.innopia.vital_sync.service.VitalService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VitalClient {

    private static VitalClient instance;
    private static Retrofit retrofit;


    private VitalClient() {}

    public static VitalClient getInstance() {
        if (instance == null) {
            instance = new VitalClient();
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

    public Response<VitalResponse> requestSyncAnalysis(double[][] RGB){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        Response<VitalResponse> response = null;
        try{
            response = retrofit.create(VitalService.class)
                    .postVitalAll(new VitalRequest(RGB, "innopiatech"))
                    .execute();
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);

        return response;
    }


    public void requestAnalysis(double[][] RGB){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        retrofit.create(VitalService.class)
                .postVitalAll(new VitalRequest(RGB, "innopiatech"))
                .enqueue(new Callback<VitalResponse>() {
                    @Override
                    public void onResponse(Call<VitalResponse> call, Response<VitalResponse> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 응답 받음
                            VitalResponse vitalResponse = response.body();

                        } else {
                            // 오류 응답 처리
                            System.out.println("Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<VitalResponse> call, Throwable t) {
                        // 네트워크 오류 등의 이유로 요청 실패
                        System.out.println("Failed to make request: " + t.getMessage());
                    }
                });

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);
    }

    public void requestHr(double[][] RGB, String mode){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        retrofit.create(VitalService.class)
                .postVitalHr(new VitalRequest(RGB, "innopiatech"))
                .enqueue(new Callback<VitalResponse>() {
                    @Override
                    public void onResponse(Call<VitalResponse> call, Response<VitalResponse> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 응답 받음
                            VitalResponse vitalResponse = response.body();

                        } else {
                            // 오류 응답 처리
                            System.out.println("Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<VitalResponse> call, Throwable t) {
                        // 네트워크 오류 등의 이유로 요청 실패
                        System.out.println("Failed to make request: " + t.getMessage());
                    }
                });

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);
    }

    public void requestHrv(double[][] RGB, String mode){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        retrofit.create(VitalService.class)
                .postVitalHrv(new VitalRequest(RGB, "innopiatech"))
                .enqueue(new Callback<VitalResponse>() {
                    @Override
                    public void onResponse(Call<VitalResponse> call, Response<VitalResponse> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 응답 받음
                            VitalResponse vitalResponse = response.body();
                        } else {
                            // 오류 응답 처리
                            System.out.println("Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<VitalResponse> call, Throwable t) {
                        // 네트워크 오류 등의 이유로 요청 실패
                        System.out.println("Failed to make request: " + t.getMessage());
                    }
                });

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);
    }
    public void requestRr(double[][] RGB, String mode){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        retrofit.create(VitalService.class)
                .postVitalRr(new VitalRequest(RGB, "innopiatech"))
                .enqueue(new Callback<VitalResponse>() {
                    @Override
                    public void onResponse(Call<VitalResponse> call, Response<VitalResponse> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 응답 받음
                            VitalResponse vitalResponse = response.body();

                        } else {
                            // 오류 응답 처리
                            System.out.println("Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<VitalResponse> call, Throwable t) {
                        // 네트워크 오류 등의 이유로 요청 실패
                        System.out.println("Failed to make request: " + t.getMessage());
                    }
                });

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);
    }
    public void requestSpo2(double[][] RGB, String mode){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        retrofit.create(VitalService.class)
                .postVitalSpo2(new VitalRequest(RGB, "innopiatech"))
                .enqueue(new Callback<VitalResponse>() {
                    @Override
                    public void onResponse(Call<VitalResponse> call, Response<VitalResponse> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 응답 받음
                            VitalResponse vitalResponse = response.body();

                        } else {
                            // 오류 응답 처리
                            System.out.println("Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<VitalResponse> call, Throwable t) {
                        // 네트워크 오류 등의 이유로 요청 실패
                        System.out.println("Failed to make request: " + t.getMessage());
                    }
                });

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);
    }
    public void requestStress(double[][] RGB, String mode){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        retrofit.create(VitalService.class)
                .postVitalStress(new VitalRequest(RGB, "innopiatech"))
                .enqueue(new Callback<VitalResponse>() {
                    @Override
                    public void onResponse(Call<VitalResponse> call, Response<VitalResponse> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 응답 받음
                            VitalResponse vitalResponse = response.body();

                        } else {
                            // 오류 응답 처리
                            System.out.println("Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<VitalResponse> call, Throwable t) {
                        // 네트워크 오류 등의 이유로 요청 실패
                        System.out.println("Failed to make request: " + t.getMessage());
                    }
                });

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);
    }
    public void requestBp(double[][] RGB, String mode){
        // 서비스 인터페이스 생성
        // 비동기적으로 POST 요청 보내기
        retrofit.create(VitalService.class)
                .postVitalBp(new VitalRequest(RGB, "innopiatech"))
                .enqueue(new Callback<VitalResponse>() {
                    @Override
                    public void onResponse(Call<VitalResponse> call, Response<VitalResponse> response) {
                        if (response.isSuccessful()) {
                            // 성공적으로 응답 받음
                            VitalResponse vitalResponse = response.body();

                        } else {
                            // 오류 응답 처리
                            System.out.println("Error: " + response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<VitalResponse> call, Throwable t) {
                        // 네트워크 오류 등의 이유로 요청 실패
                        System.out.println("Failed to make request: " + t.getMessage());
                    }
                });

        Log.d("vital", "Ready to Reqeust :: " + Config.LOCAL_SERVER_ADDRESS);
    }
    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);
}