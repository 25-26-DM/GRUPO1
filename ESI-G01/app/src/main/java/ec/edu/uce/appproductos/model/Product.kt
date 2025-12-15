package ec.edu.uce.appproductos.model

import java.util.Date

data class Product(
    val id: Int,
    val descripcion: String,
    val fechaFabricacion: Date,
    val costo: Double,
    var disponibilidad: Boolean
)
