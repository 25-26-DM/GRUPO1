package ec.edu.uce.appproductosfinal.model

data class TokenVerificationResponse(
    val success: Boolean,
    val message: String? = "",
    val usuario: User? = null,
    val token: String? = null
)
