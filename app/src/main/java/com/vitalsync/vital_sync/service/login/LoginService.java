package com.vitalsync.vital_sync.service.login;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginService {
    @GET("/login")
    Call<LoginResponse> Login(@Query("user_id") String user_id);
    @POST("/register")
    Call<LoginResponse> postRegister(@Body LoginRequest body);
    @GET("/user/list")
    Call<List<UserInfo>> getUserList();
}
