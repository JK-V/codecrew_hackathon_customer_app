package com.codecrew.app.model

import com.google.gson.annotations.SerializedName


data class CustomerData (

  @SerializedName("custId"              ) var custId              : String?  = null,
  @SerializedName("firstName"           ) var firstName           : String?  = null,
  @SerializedName("lastName"            ) var lastName            : String?  = null,
  @SerializedName("dob"                 ) var dob                 : String?  = null,
  @SerializedName("gender"              ) var gender              : String?  = null,
  @SerializedName("email"               ) var email               : String?  = null,
  @SerializedName("phoneNumber"         ) var phoneNumber         : String?  = null,
  @SerializedName("address"             ) var address             : String?  = null,
  @SerializedName("relationshipType"    ) var relationshipType    : String?  = null,
  @SerializedName("userIdentity"        ) var userIdentity        : String?  = null,
  @SerializedName("userAccessToken"     ) var userAccessToken     : String?  = null,
  @SerializedName("deviceId"            ) var deviceId            : String?  = null,
  @SerializedName("preferredDeviceFlag" ) var preferredDeviceFlag : Boolean? = null

)