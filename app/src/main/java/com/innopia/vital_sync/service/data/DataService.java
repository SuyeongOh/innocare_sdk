package com.innopia.vital_sync.service.data;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DataService {
    @GET("vital/data/vital")
    Call<List<DataResponse>> getMeasuredData(String user_id);

}
