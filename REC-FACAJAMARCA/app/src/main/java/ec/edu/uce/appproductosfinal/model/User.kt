package ec.edu.uce.appproductosfinal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val password: String = "", // Hash SHA-256
    val lastUpdated: Long = System.currentTimeMillis()
)
