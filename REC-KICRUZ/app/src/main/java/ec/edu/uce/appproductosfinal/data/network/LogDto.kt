package ec.edu.uce.appproductosfinal.data.network

data class LogDto(
    val accion: String,  // Ej: "INGRESO", "CREACION_PRODUCTO", "ELIMINACION"
    val usuario: String  // Ej: "kevin", "admin"
)