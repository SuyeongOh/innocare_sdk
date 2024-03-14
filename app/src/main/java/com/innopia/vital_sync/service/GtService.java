package com.innopia.vital_sync.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GtService {
    @POST("/vital/gt")
    Call<GtResponse> postGT(@Body GtRequest body);
}
