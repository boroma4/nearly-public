package com.ukrainianboyz.nearly.service.webAPI

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BackendService {
    private val url = "https://nearly-back.herokuapp.com/api/"
    //can have multiple services
    val backendApi: IBackendService by lazy {
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build().create(IBackendService::class.java)
    }
}