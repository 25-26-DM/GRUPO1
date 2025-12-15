package ec.edu.uce.appproductos.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ec.edu.uce.appproductos.R
import ec.edu.uce.appproductos.data.UserRepository
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit, 
    onNavigateToRegister: () -> Unit,
    showSuccessMessage: Boolean
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            snackbarHostState.showSnackbar("¡Usuario creado con éxito!")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        var nombre by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisibility by remember { mutableStateOf(false) }
        var showError by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.giovas_logo),
                contentDescription = "Logo de la app",
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, "Toggle password visibility")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val user = UserRepository.findUser(nombre, password)
                    if (user != null) {
                        onLoginSuccess(user.nombre)
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }
            if (showError) {
                Text("Usuario o contraseña incorrectos", color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onNavigateToRegister) {
                Text("¿No tienes una cuenta? Regístrate")
            }
        }
    }
}
