package ec.edu.uce.appproductos.model

data class User(
    val nombre: String,
    val apellido: String, // Lo mantenemos para el registro, pero no para el login
    val password: String
)
