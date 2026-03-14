package com.example.todoapp.data.remote

import com.example.todoapp.data.model.Todo
import retrofit2.Response
import retrofit2.http.*
import okhttp3.ResponseBody

interface TodoApiService {

    // 1. 上传待办
    @POST("api/todos/sync/upload")
    suspend fun uploadTodos(@Body todos: List<Todo>): Response<ResponseBody>

    // 2. 下载待办
    @GET("api/todos/sync/download")
    suspend fun downloadTodos(): Response<List<Todo>>

    // 3. 测试连接 - 改成 ResponseBody
    @GET("api/todos/test")
    suspend fun testConnection(): Response<ResponseBody>
}