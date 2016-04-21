package com.sam_chordas.android.stockhawk.service;

import com.sam_chordas.android.stockhawk.model.QueryResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by niharg on 3/8/16 at 6:52 PM.
 */
public interface HistoricalDataService {

    @GET("yql")
    Call<QueryResult> getHistoricalData(@Query(value = "q", encoded = true) String q, @Query("format") String format, @Query(value = "env", encoded = true) String env);

}
