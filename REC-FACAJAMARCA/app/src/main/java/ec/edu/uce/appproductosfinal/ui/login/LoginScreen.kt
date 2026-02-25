package ec.edu.uce.appproductosfinal.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ec.edu.uce.appproductosfinal.R
import ec.edu.uce.appproductosfinal.data.UserRepository
import ec.edu.uce.appproductosfinal.data.network.RetrofitClient
import ec.edu.uce.appproductosfinal.data.network.LogRequest
import ec.edu.uce.appproductosfinal.model.LoginTokenRequest
import ec.edu.uce.appproductosfinal.model.TokenVerificationRequest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    userRepository: UserRepository,
    onLoginSuccess: (String) -> Unit, 
    onNavigateToRegister: () -> Unit,
    showSuccessMessage: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Estados para el flujo de 2 pasos
    var correo by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var tokenEnviado by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.grupo1img),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (!tokenEnviado) {
                // PASO 1: Solicitar correo
                Text(
                    "Ingresa tu correo del grupo",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo Electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (correo.isBlank()) {
                                    showError = true
                                    errorMessage = "Por favor ingresa tu correo"
                                } else {
                                    isLoading = true
                                    showError = false
                                    try {
                                        val request = LoginTokenRequest(correo)
                                        val response = RetrofitClient.instance.requestLoginToken(request)
                                        if (response.isSuccessful && response.body() != null) {
                                            val body = response.body()!!
                                            val msg = body.message ?: "Error desconocido"
                                            if (body.success && body.codigoEnviado) {
                                                tokenEnviado = true
                                                successMessage = msg
                                            } else {
                                                showError = true
                                                errorMessage = msg
                                            }
                                        } else {
                                            showError = true
                                            errorMessage = "Error al enviar el código"
                                        }
                                    } catch (e: Exception) {
                                        showError = true
                                        errorMessage = "Error de conexión: ${e.message}"
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = correo.isNotBlank()
                    ) {
                        Text("Enviar Código")
                    }
                }
            } else {
                // PASO 2: Ingresar código
                Text(
                    "Verifica tu correo",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Hemos enviado un código de 6 dígitos a $correo",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.take(6) },
                    label = { Text("Código (6 dígitos)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (codigo.length != 6) {
                                    showError = true
                                    errorMessage = "El código debe tener 6 dígitos"
                                } else {
                                    isLoading = true
                                    showError = false
                                    try {
                                        val request = TokenVerificationRequest(correo, codigo)
                                        val response = RetrofitClient.instance.verifyLoginToken(request)
                                        if (response.isSuccessful && response.body() != null) {
                                            val body = response.body()!!
                                            val msg = body.message ?: "Error desconocido"
                                            if (body.success && body.usuario != null) {
                                                userRepository.addUser(body.usuario)
                                                try {
                                                    RetrofitClient.instance.registerLog(
                                                        LogRequest(
                                                            id = System.currentTimeMillis(),
                                                            tipo = "ingreso",
                                                            detalle = "origen=app; correo=$correo; usuario=${body.usuario.nombre}"
                                                        )
                                                    )
                                                } catch (_: Exception) { }
                                                onLoginSuccess(body.usuario.nombre)
                                            } else {
                                                showError = true
                                                errorMessage = msg
                                            }
                                        } else {
                                            showError = true
                                            errorMessage = "Código inválido o expirado"
                                        }
                                    } catch (e: Exception) {
                                        showError = true
                                        errorMessage = "Error de conexión: ${e.message}"
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = codigo.length == 6
                    ) {
                        Text("Verificar Código")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        tokenEnviado = false
                        correo = ""
                        codigo = ""
                        showError = false
                    },
                    enabled = !isLoading
                ) {
                    Text("Usar otro correo")
                }
            }

            if (showError) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            if (successMessage.isNotEmpty() && tokenEnviado) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(successMessage, color = MaterialTheme.colorScheme.primary)
            }

            if (!tokenEnviado) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
                    Text("¿No tienes acceso? Regístrate aquí")
                }
            }
        }
    }
}
