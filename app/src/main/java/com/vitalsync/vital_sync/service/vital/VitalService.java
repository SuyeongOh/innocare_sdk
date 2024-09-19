package com.vitalsync.vital_sync.service.vital;

import com.vitalsync.vital_sync.service.ecg.EcgRequest;
import com.vitalsync.vital_sync.service.ecg.EcgResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VitalService {
    @POST("/vital/all")
    Call<VitalResponse> postVitalAll(@Body VitalRequest body);
    @POST("/vital/hr")
    Call<VitalResponse> postVitalHr(@Body VitalRequest body);
    @POST("/vital/hrv")
    Call<VitalResponse> postVitalHrv(@Body VitalRequest body);
    @POST("/vital/rr")
    Call<VitalResponse> postVitalRr(@Body VitalRequest body);
    @POST("/vital/spo2")
    Call<VitalResponse> postVitalSpo2(@Body VitalRequest body);
    @POST("/vital/stress")
    Call<VitalResponse> postVitalStress(@Body VitalRequest body);
    @POST("/vital/bp")
    Call<VitalResponse> postVitalBp(@Body VitalRequest body);
    @POST("/vital/verity")
    Call<EcgResponse> postVitalVerity(@Body EcgRequest body);
}