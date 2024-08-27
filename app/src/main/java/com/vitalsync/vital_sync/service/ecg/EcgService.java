package com.vitalsync.vital_sync.service.ecg;

import retrofit2.Call;
import retrofit2.http.POST;

public interface EcgService {
    @POST("/ecg")
    Call<EcgResponse> postEcgSignal(EcgRequest request);
}
