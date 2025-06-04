package com.codecrew.app.model

import com.codecrew.app.model.RetrofitClient.BASE_URL
import com.codecrew.app.model.RetrofitClient.gson
import com.codecrew.app.model.RetrofitClient.okHttpClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

	    private const val TIME_OUT: Long = 120

	     val gson = GsonBuilder().setLenient().create()

	const val BASE_URL = "https://bankinsta-fqbnfqaeacbae5cd.southindia-01.azurewebsites.net"

	 val loggingInterceptor = HttpLoggingInterceptor().apply {
		level = HttpLoggingInterceptor.Level.BODY // Logs URL, method, headers, and request/response bodies
	}
	     val okHttpClient = OkHttpClient.Builder()
	        .readTimeout(TIME_OUT, TimeUnit.SECONDS)
	        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
			.addInterceptor(loggingInterceptor)
//	        .addInterceptor { chain ->
//	            val resp = chain.proceed(chain.request())
//	            // Deal with the response code
//	            if (resp.code == 200) {
//	                try {
//	                    val myJson = resp.peekBody(2048).string() // peekBody() will not close the response
//	                    println(myJson)
//	                } catch (e: Exception) {
//	                    println("--Jaye Error parse json from intercept..............")
//	                }
//	            } else {
//	                println(resp)
//	            }
//	            resp
//	        }
			.build()

//	    val retrofit: RetrofitInterface by lazy {
//	        Retrofit.Builder()
//	            .addConverterFactory(GsonConverterFactory.create(gson))
//	            .baseUrl(BASE_URL)
//	            .client(okHttpClient)
//	            .build().create(RetrofitInterface::class.java)
//	    }

	fun create(): RetrofitInterface {
		return Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create(gson))
			.baseUrl(BASE_URL)
			.client(okHttpClient)
			.build()
			.create(RetrofitInterface::class.java)
	}

	}