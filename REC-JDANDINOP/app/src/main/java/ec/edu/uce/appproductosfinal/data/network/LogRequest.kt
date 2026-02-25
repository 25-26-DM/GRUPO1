package ec.edu.uce.appproductosfinal.data.network

data class LogRequest(
    val accion: String,  // "ingreso", "creacion", "actualizacion", "eliminacion"
    val usuario: String,
    val fecha: String
)