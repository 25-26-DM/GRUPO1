package ec.edu.uce.appproductosfinal.data.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LogRecApiService {
    @POST("LogRecService")
    fun enviarLog(@Body request: LogRequest): Call<ResponseBody>
}
