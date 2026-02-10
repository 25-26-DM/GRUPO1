package ec.edu.uce.appproductosfinal

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
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
import ec.edu.uce.appproductosfinal.location.SharedPreferenceUtil
import ec.edu.uce.appproductosfinal.ui.home.HomeScreen
import ec.edu.uce.appproductosfinal.ui.login.LoginScreen
import ec.edu.uce.appproductosfinal.ui.product.ProductScreen
import ec.edu.uce.appproductosfinal.ui.register.RegisterScreen
import ec.edu.uce.appproductosfinal.ui.theme.AppProductosTheme
import kotlinx.coroutines.delay

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
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val productRepository = remember { ProductRepository(database.productDao()) }

    val savedUser = remember { SharedPreferenceUtil.getUserSession(context) }
    // Nota: Para sesiones persistentes, podrías necesitar guardar también el lastLogin en SharedPreferences
    val startRoute = if (savedUser != null) "home/$savedUser/Sesión persistente" else "login"

    val navController = rememberNavController()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSessionExpiredDialog by remember { mutableStateOf(false) }

    val logout = {
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

    // ... (Diálogos de Sesión Expirada y Logout se mantienen igual)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    SharedPreferenceUtil.updateLastActivity(context)
                }
                false
            }
    ) {
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
                    onLoginSuccess = { userName, lastLogin ->
                        // Guardamos la sesión
                        SharedPreferenceUtil.saveUserSession(context, userName)
                        // Navegamos pasando AMBOS parámetros
                        navController.navigate("home/$userName/$lastLogin") {
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
                        navController.navigate("login?showSuccess=true") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "home/{userName}/{lastLogin}", // Nueva ruta con lastLogin
                arguments = listOf(
                    navArgument("userName") { type = NavType.StringType },
                    navArgument("lastLogin") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userName = backStackEntry.arguments?.getString("userName") ?: ""
                val lastLogin = backStackEntry.arguments?.getString("lastLogin") ?: ""

                HomeScreen(
                    userName = userName,
                    lastLogin = lastLogin, // Pasamos la fecha al HomeScreen
                    productRepository = productRepository,
                    onLogout = { showLogoutDialog = true },
                    onAddProduct = { navController.navigate("product") },
                    onEditProduct = { product -> navController.navigate("product?id=${product.id}") },
                    onDeleteProduct = { },
                    onNavigateToSensors = { },
                    onNavigateToLocation = { },
                    onBack = { }
                )
            }

            composable("product?id={id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
                ProductScreen(
                    productId = id,
                    productRepository = productRepository,
                    onSave = { navController.popBackStack() }
                )
            }
        }
    }
}