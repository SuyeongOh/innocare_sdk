package com.vitalsync.vital_sync.service.gt;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GtService {
    @POST("/vital/gt")
    Call<GtResponse> postGT(@Body GtRequest body);
}
