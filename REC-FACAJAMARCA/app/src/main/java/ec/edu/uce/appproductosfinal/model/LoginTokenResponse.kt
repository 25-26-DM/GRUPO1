package ec.edu.uce.appproductosfinal.model

data class LoginTokenResponse(
    val success: Boolean,
    val message: String? = "",
    val codigoEnviado: Boolean = false
)
