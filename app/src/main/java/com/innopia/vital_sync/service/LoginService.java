package com.innopia.vital_sync.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginService {
    @POST("/login")
    Call<LoginResponse> postLogin(@Body LoginRequest body);
    @POST("/register")
    Call<LoginResponse> postRegister(@Body LoginRequest body);
}
