package com.hotukrainianboyz.nearly.service.webAPI

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BackendService {
    private val url = "http://13.53.90.64:8080/api/"
    //can have multiple services
    val backendApi: IBackendService by lazy {
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(IBackendService::class.java)
    }
}