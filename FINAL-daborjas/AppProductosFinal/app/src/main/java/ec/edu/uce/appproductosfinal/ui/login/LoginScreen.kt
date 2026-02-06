package ec.edu.uce.appproductosfinal.ui.login

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ec.edu.uce.appproductosfinal.R
import ec.edu.uce.appproductosfinal.data.UserRepository
import ec.edu.uce.appproductosfinal.data.network.RetrofitClient
import ec.edu.uce.appproductosfinal.utils.SecurityUtils
import kotlinx.coroutines.delay
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

    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }

    // 1. THE ETHEREAL ELEVATION ANIMATION STATE
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
    }

    // 2. THE BREATHING LOGO ANIMATION
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(2000), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Logo
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -40 }),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.grupo1img),
                        contentDescription = "Logo",
                        modifier = Modifier.size(200.dp).scale(logoScale)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Animated Form Elements
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(tween(800, delayMillis = 300)) + slideInVertically(initialOffsetY = { 40 }),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Bienvenido",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Introduce tus credenciales para continuar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.alpha(0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Usuario") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isChecking,
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isChecking,
                            shape = RoundedCornerShape(16.dp),
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                    Icon(if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Action Button with Morphing State
                        if (isChecking) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        } else {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isChecking = true
                                        showError = false
                                        val hashedPassword = SecurityUtils.hashPassword(password)
                                        val localUser = userRepository.findUser(nombre, hashedPassword)
                                        if (localUser != null) {
                                            onLoginSuccess(localUser.nombre)
                                        } else {
                                            try {
                                                val response = RetrofitClient.instance.getUser(nombre)
                                                if (response.isSuccessful && response.body() != null) {
                                                    val cloudUser = response.body()!!
                                                    if (cloudUser.password == hashedPassword) {
                                                        userRepository.addUser(cloudUser)
                                                        onLoginSuccess(cloudUser.nombre)
                                                    } else { showError = true }
                                                } else { showError = true }
                                            } catch (e: Exception) { showError = true }
                                        }
                                        isChecking = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Acceder", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        if (showError) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Credenciales incorrectas", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onNavigateToRegister, enabled = !isChecking) {
                            Text("¿No tienes cuenta? Crea una ahora", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
