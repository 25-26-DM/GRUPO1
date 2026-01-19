package ec.edu.uce.appproductos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ec.edu.uce.appproductos.data.AppDatabase
import ec.edu.uce.appproductos.data.ProductRepository
import ec.edu.uce.appproductos.data.UserRepository
import ec.edu.uce.appproductos.ui.home.HomeScreen
import ec.edu.uce.appproductos.ui.login.LoginScreen
import ec.edu.uce.appproductos.ui.product.ProductScreen
import ec.edu.uce.appproductos.ui.register.RegisterScreen
import ec.edu.uce.appproductos.ui.sensor.SensorScreen
import ec.edu.uce.appproductos.ui.location.LocationScreen
import ec.edu.uce.appproductos.ui.theme.AppProductosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppProductosTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(database.userDao()) }
    val productRepository = remember { ProductRepository(database.productDao()) }
    val coroutineScope = rememberCoroutineScope()

    val navController = rememberNavController()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirmar Cierre de Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar la sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    NavHost(navController = navController, startDestination = "login") {
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
                onLoginSuccess = { userName -> navController.navigate("home/$userName") },
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
            route = "home/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            HomeScreen(
                userName = userName,
                productRepository = productRepository,
                onLogout = {
                    showLogoutDialog = true
                },
                onAddProduct = { navController.navigate("product") },
                onEditProduct = { product -> navController.navigate("product?id=${product.id}") },
                onDeleteProduct = { product ->
                    coroutineScope.launch {
                        productRepository.deleteProduct(product.id)
                        navController.navigate("home/$userName") {
                            popUpTo("home/$userName") { inclusive = true }
                        }
                    }
                },
                onNavigateToSensors = { navController.navigate("sensors") },
                onNavigateToLocation = { navController.navigate("location") }
            )
        }
        composable("product?id={id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            ProductScreen(
                productId = id,
                productRepository = productRepository,
                onSave = {
                    navController.popBackStack()
                }
            )
        }
        composable("sensors") {
            SensorScreen(onBack = { navController.popBackStack() })
        }
        composable("location") {
            LocationScreen(onBack = { navController.popBackStack() })
        }
    }
}
