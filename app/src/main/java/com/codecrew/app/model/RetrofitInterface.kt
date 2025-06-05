package com.codecrew.app.model

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
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

	@PATCH("/bapp/{custId}")
	suspend fun updatePreferDevice(
		@Path("custId") custId: String?,
		@Header("Content-Type") header: String = "application/json",
		@Header("host") host: String = "bankinsta-fqbnfqaeacbae5cd.southindia-01.azurewebsites.net",
		@Body customerData: CustomerData): CustomerData
}