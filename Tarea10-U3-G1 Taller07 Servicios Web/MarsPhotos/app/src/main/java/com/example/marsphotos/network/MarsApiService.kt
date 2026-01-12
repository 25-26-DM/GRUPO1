package com.example.marsphotos.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

private const val BASE_URL =
    "https://android-kotlin-fun-mars-server.appspot.com"

/**
 * Utiliza el constructor de Retrofit para crear un objeto retrofit con un convertidor de kotlinx.serialization.
 */
private val retrofit = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

/**
 * Una interfaz de servicio que define cómo Retrofit se comunica con el servidor web utilizando
 * métodos HTTP como GET, POST, PUT, DELETE.
 */
interface MarsApiService {
    /**
     * Devuelve una [List] de [MarsPhoto] y este método puede llamarse desde una corrutina.
     * La anotación @GET indica que el endpoint "photos" se consultará con el método HTTP GET
     */
    @GET("photos")
    suspend fun getPhotos(): List<MarsPhoto>
}

/**
 * Un objeto de API público que expone el servicio de Retrofit inicializado de forma diferida (lazy).
 */
object MarsApi {
    val retrofitService: MarsApiService by lazy {
        retrofit.create(MarsApiService::class.java)
    }
}
