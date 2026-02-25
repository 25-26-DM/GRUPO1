package ec.edu.uce.appproductosfinal.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ec.edu.uce.appproductosfinal.R
import ec.edu.uce.appproductosfinal.data.AppDatabase
import ec.edu.uce.appproductosfinal.data.ProductRepository
import ec.edu.uce.appproductosfinal.model.Product
import java.io.ByteArrayOutputStream
import java.io.InputStream

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ProductRepository(database.productDao())
        
        return try {
            val localProducts = repository.getProducts()
            var syncCount = 0
            
            for (product in localProducts) {
                // Si el producto ya tiene un link de S3 (empieza con http), lo saltamos
                if (product.imageUri?.startsWith("http") == true) continue

                try {
                    val productDto = product.toDto(applicationContext)
                    val response = RetrofitClient.instance.syncProduct(productDto)

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        
                        // Si el servidor retorna la URL de S3, la guardamos localmente
                        val cloudUrl = if (!responseBody?.url.isNullOrEmpty()) {
                            responseBody!!.url
                        } else {
                            "http://cloud.sync/success/${product.id}"
                        }

                        repository.updateProduct(product.copy(imageUri = cloudUrl))
                        syncCount++
                        Log.d("SyncWorker", "Producto ${product.id} sincronizado: $cloudUrl")
                    } else {
                        Log.e("SyncWorker", "Error servidor en ID ${product.id}: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("SyncWorker", "Error de red en ID ${product.id}: ${e.message}")
                }
            }

            if (syncCount > 0) showNotification(repository.getProducts().size)
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error fatal en worker", e)
            Result.retry()
        }
    }

    private fun showNotification(totalLocalCount: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "inventory_sync_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Sync", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Sincronización Exitosa")
            .setContentText("Tu inventario de $totalLocalCount productos está al día.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        notificationManager.notify(1, notification)
    }

    private fun Product.toDto(context: Context): ProductDto {
        var base64: String? = null
        if (!imageUri.isNullOrEmpty() && !imageUri!!.startsWith("http")) {
            try {
                val uri = Uri.parse(imageUri)
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    
                    if (bitmap != null) {
                        // Redimensionar para no exceder límites de AWS (800px ancho)
                        val width = 800
                        val height = (width * (bitmap.height.toFloat() / bitmap.width)).toInt()
                        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)
                        
                        val outputStream = ByteArrayOutputStream()
                        resized.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                        
                        // Convertir a Base64 sin saltos de línea
                        base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                        Log.d("SyncWorker", "Imagen convertida a Base64 (${base64.length} chars)")
                    } else {
                        Log.e("SyncWorker", "No se pudo decodificar el bitmap de $imageUri")
                    }
                } else {
                    Log.e("SyncWorker", "No se pudo abrir stream para $imageUri")
                }
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error en procesamiento de imagen", e)
            }
        }
        return ProductDto(id, descripcion, fechaFabricacion, costo, disponibilidad, imageUri, lastUpdated, base64)
    }
}
