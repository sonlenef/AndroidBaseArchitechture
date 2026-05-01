package dev.sonle.androidbasearchitecture.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.sonle.androidbasearchitecture.core.config.EnvironmentConfig
import dev.sonle.androidbasearchitecture.core.network.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Module providing network-related dependencies
 */
val networkModule = module {
    
    single {
        GsonBuilder()
            .setLenient()
            .create()
    }
    
    single {
        HttpLoggingInterceptor { message ->
            if (EnvironmentConfig.enableDebugLogging) {
                Timber.d("HTTP: $message")
            }
        }.apply {
            level = if (EnvironmentConfig.enableDebugLogging) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(EnvironmentConfig.apiTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(EnvironmentConfig.apiTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(EnvironmentConfig.apiTimeoutSeconds.toLong(), TimeUnit.SECONDS)
            .build()
    }
    
    single {
        Retrofit.Builder()
            .baseUrl(EnvironmentConfig.apiBaseUrl)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
            .build()
    }
    
    single {
        get<Retrofit>().create(ApiService::class.java)
    }
}
