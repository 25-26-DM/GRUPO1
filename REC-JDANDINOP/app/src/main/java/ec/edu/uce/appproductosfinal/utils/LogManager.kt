package ec.edu.uce.appproductosfinal.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import ec.edu.uce.appproductosfinal.data.network.LogRecApiService
import ec.edu.uce.appproductosfinal.data.network.LogRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

object LogManager {
    private const val TAG = "LogManager"
    private const val BASE_URL = "https://qnhh3yf3bl.execute-api.us-east-1.amazonaws.com/default/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(LogRecApiService::class.java)

    /**
     * Registra una acción en el servicio de logs de AWS DynamoDB.
     * @param accion Tipo de acción: "ingreso", "creacion", "actualizacion", "eliminacion"
     * @param usuario Nombre del usuario que realiza la acción
     */
    fun registrarLog(context: Context, accion: String, usuario: String) {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logData = LogRequest(accion, usuario, fechaActual)
        
        // Handler para mostrar Toasts en el hilo principal
        val mainHandler = Handler(Looper.getMainLooper())

        Log.d(TAG, "Intentando enviar log a: ${BASE_URL}LogRecService")

        service.enviarLog(logData).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d(TAG, "Log registrado correctamente: $responseBody")
                    // Confirmación visual solo para depuración rápida
                    mainHandler.post { 
                        Toast.makeText(context, "Log AWS Enviado OK", Toast.LENGTH_SHORT).show() 
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Error del servidor (${response.code()}): $errorBody")
                    mainHandler.post { 
                        Toast.makeText(context, "Error AWS: ${response.code()}", Toast.LENGTH_LONG).show() 
                    }
                }
            }

            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                Log.e(TAG, "Fallo crítico de conexión: ${t.message}", t)
                mainHandler.post { 
                    Toast.makeText(context, "Fallo Red AWS: ${t.localizedMessage}", Toast.LENGTH_LONG).show() 
                }
            }
        })
    }
}
