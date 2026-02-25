package ec.edu.uce.appproductosfinal.model

data class TokenVerificationRequest(
    val correo: String,
    val codigo: String
)
