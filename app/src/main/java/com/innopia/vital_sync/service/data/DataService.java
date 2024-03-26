package com.innopia.vital_sync.service.data;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DataService {
    @GET("vital/data/vital")
    Call<List<DataResponse>> getMeasuredData(@Query("user_id") String user_id);
}
