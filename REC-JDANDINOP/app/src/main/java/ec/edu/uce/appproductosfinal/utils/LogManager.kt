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

/**
 * LogManager consolidado: Maneja el envío de logs a AWS DynamoDB.
 * Usa las definiciones centrales en ec.edu.uce.appproductosfinal.data.network.
 */
object LogManager {
    private const val TAG = "AWS_LOG"
    private const val BASE_URL = "https://qnhh3yf3bl.execute-api.us-east-1.amazonaws.com/default/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(LogRecApiService::class.java)

    /**
     * Registra una acción de usuario en AWS.
     * @param context Contexto de Android (para mostrar Toasts de depuración)
     * @param accion "ingreso", "creacion", "actualizacion", "eliminacion"
     * @param usuario Nombre del usuario
     */
    fun registrarLog(context: Context, accion: String, usuario: String) {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val logData = LogRequest(accion, usuario, fechaActual)
        
        val mainHandler = Handler(Looper.getMainLooper())

        Log.d(TAG, "Intentando enviar log: $accion por $usuario")

        service.enviarLog(logData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Log registrado exitosamente en AWS")
                    mainHandler.post { 
                        Toast.makeText(context, "Log AWS: OK ($accion)", Toast.LENGTH_SHORT).show() 
                    }
                } else {
                    Log.e(TAG, "Error servidor AWS (${response.code()}): ${response.errorBody()?.string()}")
                    mainHandler.post { 
                        Toast.makeText(context, "Error AWS: ${response.code()}", Toast.LENGTH_LONG).show() 
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "Fallo red AWS: ${t.message}")
                mainHandler.post { 
                    Toast.makeText(context, "Fallo Red AWS: ${t.localizedMessage}", Toast.LENGTH_LONG).show() 
                }
            }
        })
    }
}
