package dev.sonle.androidbasearchitechture.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for User from API
 */
data class UserDto(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String,
    
    @SerializedName("website")
    val website: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("address")
    val address: AddressDto? = null,
    
    @SerializedName("company")
    val company: CompanyDto? = null
)

/**
 * Address DTO
 */
data class AddressDto(
    @SerializedName("street")
    val street: String,
    
    @SerializedName("suite")
    val suite: String,
    
    @SerializedName("city")
    val city: String,
    
    @SerializedName("zipcode")
    val zipcode: String,
    
    @SerializedName("geo")
    val geo: GeoDto? = null
)

/**
 * Geo coordinates DTO
 */
data class GeoDto(
    @SerializedName("lat")
    val lat: String,
    
    @SerializedName("lng")
    val lng: String
)

/**
 * Company DTO
 */
data class CompanyDto(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("catchPhrase")
    val catchPhrase: String,
    
    @SerializedName("bs")
    val bs: String
)
