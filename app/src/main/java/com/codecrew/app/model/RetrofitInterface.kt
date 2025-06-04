package com.codecrew.app.model

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RetrofitInterface {

    	@GET("/bapp/{custId}")
    	suspend fun getCustomerData(@Path("custId") custId: String): CustomerData

		@POST("/bapp")
		suspend fun signupCustomer(
			@Header("Content-Type") header: String = "application/json",
			@Header("host") host: String = "bankinsta-fqbnfqaeacbae5cd.southindia-01.azurewebsites.net",
			@Body customerData: CustomerData): CustomerData
	}