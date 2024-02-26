package com.innopia.vital_sync.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VitalService {
    @POST("/vital/all")
    Call<VitalResponse> postVitalAll(@Body VitalRequest body);
}