package ec.edu.uce.appproductosfinal

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ec.edu.uce.appproductosfinal.data.AppDatabase
import ec.edu.uce.appproductosfinal.data.ProductRepository
import ec.edu.uce.appproductosfinal.data.UserRepository
import ec.edu.uce.appproductosfinal.data.network.LogDto
import ec.edu.uce.appproductosfinal.data.network.RetrofitClient
import ec.edu.uce.appproductosfinal.location.SharedPreferenceUtil
import ec.edu.uce.appproductosfinal.ui.home.HomeScreen
import ec.edu.uce.appproductosfinal.ui.login.LoginScreen
import ec.edu.uce.appproductosfinal.ui.product.ProductScreen
import ec.edu.uce.appproductosfinal.ui.register.RegisterScreen
import ec.edu.uce.appproductosfinal.ui.theme.AppProductosTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppProductosTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { }
                )
                
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                AppNavigation()
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // --- ESTADO PARA LA BURBUJA DE TEXTO (SNACKBAR) ---
    val snackbarHostState = remember { SnackbarHostState() }

    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val productRepository = remember { ProductRepository(database.productDao()) }

    val savedUser = remember { SharedPreferenceUtil.getUserSession(context) }
    val startRoute = remember(savedUser) {
        if (savedUser != null) {
            val encoded = URLEncoder.encode(savedUser, StandardCharsets.UTF_8.toString())
            "home/$encoded"
        } else {
            "login"
        }
    }
    val navController = rememberNavController()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSessionExpiredDialog by remember { mutableStateOf(false) }

    // --- FUNCIÓN MAESTRA DE AUDITORÍA ---
    val auditAction: (String, String) -> Unit = { accion, usuario ->
        scope.launch {
            // 1. Mostrar Burbuja Visual
            snackbarHostState.showSnackbar(
                message = "Registrando: $accion - Usuario: $usuario",
                withDismissAction = true
            )
            
            // 2. Enviar a la Nube (DynamoDB/AWS)
            launch(Dispatchers.IO) {
                try {
                    val log = LogDto(accion = accion, usuario = usuario)
                    RetrofitClient.instance.registrarLog(log)
                    Log.d("AUDITORIA", "Log enviado exitosamente: $accion")
                } catch (e: Exception) {
                    Log.e("AUDITORIA", "Error enviando log", e)
                }
            }
        }
    }

    val logout = {
        val user = SharedPreferenceUtil.getUserSession(context) ?: "Desconocido"
        auditAction("SALIDA", user) // Log de salida
        SharedPreferenceUtil.clearSession(context)
        navController.navigate("login") {
            popUpTo(0) { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (SharedPreferenceUtil.getUserSession(context) != null) {
                if (!SharedPreferenceUtil.isSessionValid(context)) {
                    showSessionExpiredDialog = true
                }
            }
            delay(5000)
        }
    }

    // Diálogos y Scaffold Global para mostrar el Snackbar
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInteropFilter { motionEvent ->
                    if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                        SharedPreferenceUtil.updateLastActivity(context)
                    }
                    false
                }
        ) {
            
            if (showSessionExpiredDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    title = { Text("Sesión Expirada") },
                    text = { Text("Tu sesión ha expirado por inactividad.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showSessionExpiredDialog = false
                            logout()
                        }) { Text("OK") }
                    }
                )
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Cerrar Sesión") },
                    text = { Text("¿Deseas salir de la aplicación?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            logout()
                        }) { Text("Confirmar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
                    }
                )
            }

            NavHost(navController = navController, startDestination = startRoute) {
                composable(
                    route = "login?showSuccess={showSuccess}",
                    arguments = listOf(navArgument("showSuccess") { 
                        type = NavType.BoolType
                        defaultValue = false 
                    })
                ) { backStackEntry ->
                    val showSuccess = backStackEntry.arguments?.getBoolean("showSuccess") ?: false
                    LoginScreen(
                        userRepository = userRepository,
                        onLoginSuccess = { userName -> 
                            auditAction("INGRESO", userName)
                            SharedPreferenceUtil.saveUserSession(context, userName)
                            val encodedName = URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
                            navController.navigate("home/$encodedName") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate("register") },
                        showSuccessMessage = showSuccess
                    )
                }
                
                composable("register") {
                    RegisterScreen(
                        userRepository = userRepository,
                        onRegisterSuccess = {
                            auditAction("CREACION_USUARIO", "NuevoUsuario")
                            navController.navigate("login?showSuccess=true") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(
                    route = "home/{userName}",
                    arguments = listOf(navArgument("userName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userName = backStackEntry.arguments?.getString("userName") ?: ""
                    HomeScreen(
                        userName = userName,
                        productRepository = productRepository,
                        onLogout = { showLogoutDialog = true },
                        onAddProduct = { navController.navigate("product?user=$userName") },
                        onEditProduct = { product -> navController.navigate("product?id=${product.id}&user=$userName") },
                        onDeleteAction = { auditAction("ELIMINACION", userName) }
                    )
                }
                
                composable(
                    route = "product?id={id}&user={user}",
                    arguments = listOf(
                        navArgument("id") { nullable = true },
                        navArgument("user") { type = NavType.StringType; defaultValue = "Admin" }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                    val user = backStackEntry.arguments?.getString("user") ?: "Admin"
                    
                    ProductScreen(
                        productId = id,
                        productRepository = productRepository,
                        onSave = {
                            val accion = if (id == null) "CREACION" else "ACTUALIZACION"
                            auditAction(accion, user)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
