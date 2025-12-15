package ec.edu.uce.appproductos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ec.edu.uce.appproductos.data.ProductRepository
import ec.edu.uce.appproductos.ui.home.HomeScreen
import ec.edu.uce.appproductos.ui.login.LoginScreen
import ec.edu.uce.appproductos.ui.product.ProductScreen
import ec.edu.uce.appproductos.ui.register.RegisterScreen
import ec.edu.uce.appproductos.ui.theme.AppProductosTheme

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
                onLoginSuccess = { userName -> navController.navigate("home/$userName") },
                onNavigateToRegister = { navController.navigate("register") },
                showSuccessMessage = showSuccess
            )
        }
        composable("register") {
            RegisterScreen(
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
                onLogout = {
                    showLogoutDialog = true
                },
                onAddProduct = { navController.navigate("product") },
                onEditProduct = { product -> navController.navigate("product?id=${product.id}") },
                onDeleteProduct = { product ->
                    ProductRepository.deleteProduct(product.id)
                    navController.navigate("home/$userName") {
                        popUpTo("home/$userName") { inclusive = true }
                    }
                }
            )
        }
        composable("product?id={id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            val product = id?.let { ProductRepository.getProducts().find { p -> p.id == it } }
            ProductScreen(
                product = product,
                onSave = {
                    navController.popBackStack()
                }
            )
        }
    }
}
