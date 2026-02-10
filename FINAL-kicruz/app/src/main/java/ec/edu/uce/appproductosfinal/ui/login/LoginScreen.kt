package ec.edu.uce.appproductosfinal.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ec.edu.uce.appproductosfinal.R
import ec.edu.uce.appproductosfinal.data.UserRepository
import ec.edu.uce.appproductosfinal.data.network.RetrofitClient
import ec.edu.uce.appproductosfinal.utils.SecurityUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LoginScreen(
    userRepository: UserRepository,
    onLoginSuccess: (String, String) -> Unit, // Cambiado para pasar nombre y último login
    onNavigateToRegister: () -> Unit,
    showSuccessMessage: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }

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

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isChecking,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isChecking,
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isChecking) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isChecking = true
                            showError = false
                            val hashedPassword = SecurityUtils.hashPassword(password)

                            // 1. Intentar buscar usuario localmente
                            val localUser = userRepository.findUser(nombre, hashedPassword)

                            val finalUser = localUser ?: try {
                                // 2. Si no está local, buscar en API
                                val response = RetrofitClient.instance.getUser(nombre)
                                if (response.isSuccessful && response.body()?.password == hashedPassword) {
                                    response.body()?.also { userRepository.addUser(it) }
                                } else null
                            } catch (e: Exception) { null }

                            if (finalUser != null) {
                                // 3. Lógica de Fecha: Obtener la actual para la DB
                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                val now = sdf.format(Date())

                                // El "Último acceso" es lo que ya estaba guardado antes de esta sesión
                                val displayLastLogin = finalUser.lastLogin ?: "Primer ingreso"

                                // 4. Actualizar la base de datos con la nueva fecha
                                userRepository.updateLastLogin(finalUser.nombre, now)

                                // 5. Éxito: Pasamos el nombre y la fecha que debe mostrar el Header
                                onLoginSuccess(finalUser.nombre, displayLastLogin)
                            } else {
                                showError = true
                            }
                            isChecking = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Iniciar Sesión")
                }
            }

            if (showError) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Credenciales inválidas", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onNavigateToRegister, enabled = !isChecking) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}