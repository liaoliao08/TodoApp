package com.example.todoapp.data.remote


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val DEBUG = true
    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            // 日志拦截器，方便调试
            level = if (DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .connectTimeout(30, TimeUnit.SECONDS)  // 连接超时
        .readTimeout(30, TimeUnit.SECONDS)     // 读取超时
        .writeTimeout(30, TimeUnit.SECONDS)    // 写入超时
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: TodoApiService = retrofit.create(TodoApiService::class.java)
}