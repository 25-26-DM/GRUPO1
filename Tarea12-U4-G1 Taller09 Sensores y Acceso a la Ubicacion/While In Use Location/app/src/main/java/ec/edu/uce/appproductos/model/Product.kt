package ec.edu.uce.appproductos.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val descripcion: String,
    val fechaFabricacion: Date,
    val costo: Double,
    var disponibilidad: Boolean
)
