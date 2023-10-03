package com.example.myapplication.api

import com.example.myapplication.dataClasses.BookBus
import com.example.myapplication.dataClasses.Coordinate
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
interface ApiService {

    @GET("case-study/6/stations/")
    fun getCoordinates(): Call<List<Coordinate>>

    @POST("case-study/6/stations/{station_id}/trips/{trip_id}")
    fun sendPostRequest(
        @Path("station_id") stationId: Int,
        @Path("trip_id") tripId: Int,
        @Body postData: BookBus
    ): Call<Void>
}

