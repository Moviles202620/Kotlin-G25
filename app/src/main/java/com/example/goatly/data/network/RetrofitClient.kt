package com.example.goatly.data.network

import android.content.Context
import com.squareup.moshi.Moshi
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // private const val BASE_URL = "http://172.20.10.2:8000/" // IP Hotspot Isabella Lozano
    private const val BASE_URL = "http://10.0.2.2:8000/" // IP emulador

    // Sprint 3 Literal 7 — Layer 3: OkHttp HTTP Cache (10 MB)
    private var httpCache: Cache? = null

    fun init(context: Context) {
        httpCache = Cache(
            directory = File(context.cacheDir, "http_cache"),
            maxSize = 10L * 1024L * 1024L  // 10 MB
        )
    }

    private val moshi = Moshi.Builder().build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Interceptor that adds Cache-Control to BQ endpoint responses (1 hour)
    // BQ data comes from BigQuery and doesn't change frequently
    private val cacheControlInterceptor = okhttp3.Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (chain.request().url.encodedPath.contains("bq/top-offers")) {
            response.newBuilder()
                .header("Cache-Control", "public, max-age=3600")
                .build()
        } else {
            response
        }
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cache(httpCache)
            .addInterceptor(logging)
            .addNetworkInterceptor(cacheControlInterceptor)
            .connectionPool(okhttp3.ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
