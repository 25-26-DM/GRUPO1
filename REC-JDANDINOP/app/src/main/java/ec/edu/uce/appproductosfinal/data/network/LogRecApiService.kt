package ec.edu.uce.appproductosfinal.data.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LogRecApiService {
    @POST("LogRecService") // Ojo: Si tu URL termina en /default/LogRecService, pon solo la parte final o ajusta la BaseURL
    fun enviarLog(@Body request: LogRequest): Call<Void>
}