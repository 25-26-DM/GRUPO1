package ec.edu.uce.appproductosfinal.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox // ✅ Usamos el nativo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.work.*
import coil.compose.rememberAsyncImagePainter
import ec.edu.uce.appproductosfinal.data.ProductRepository
import ec.edu.uce.appproductosfinal.data.network.RetrofitClient
import ec.edu.uce.appproductosfinal.data.network.SyncWorker
import ec.edu.uce.appproductosfinal.model.Product
import ec.edu.uce.appproductosfinal.utils.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    productRepository: ProductRepository,
    onLogout: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val products by productRepository.getProductsFlow().collectAsState(initial = emptyList())

    var isRefreshing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "EC")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Saludo
    val greeting = remember {
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "¡Buenos días!"
            in 12..18 -> "¡Buenas tardes!"
            else -> "¡Buenas noches!"
        }
    }

    // Función de Recarga
    val refreshData = {
        scope.launch(Dispatchers.IO) {
            isRefreshing = true
            try {
                // 1. Lógica de Nube
                val response = RetrofitClient.instance.getAllProducts()
                if (response.isSuccessful) {
                    val cloudProducts = response.body() ?: emptyList()
                    val localProducts = productRepository.getProducts()

                    cloudProducts.forEach { cloud ->
                        val local = localProducts.find { it.id == cloud.id }
                        if (local == null) {
                            productRepository.addProduct(cloud)
                        } else if (cloud.lastUpdated > local.lastUpdated) {
                            productRepository.updateProduct(cloud)
                        }
                    }
                }

                // 2. Worker
                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                WorkManager.getInstance(context).enqueue(syncRequest)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        // ✅ CORRECCIÓN: Usamos PullToRefreshBox nativo de Material3
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { refreshData() },
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            ) {
                // 1. Header con Gradiente
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp) // Un poco más alto para que quepa todo bien
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                ),
                                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            // Barra superior
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = greeting,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = userName,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                IconButton(
                                    onClick = onLogout,
                                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Tarjeta de Resumen (Dentro del Box del header)
                            SummaryCard(products.size, products.sumOf { it.costo }, currencyFormat)
                        }
                    }
                }

                // 2. Título
                item {
                    Text(
                        text = "Tus Productos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
                    )
                }

                // 3. Lista o Vacío
                if (products.isEmpty()) {
                    item { EmptyState() }
                } else {
                    items(products, key = { it.id }) { product ->
                        ProductItem(
                            product = product,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat,
                            onEdit = { onEditProduct(product) },
                            onDelete = {
                                productToDelete = product
                                showDeleteDialog = true
                            }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Diálogo de Eliminación (Sin cambios)
    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar '${productToDelete?.descripcion}'?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        val prod = productToDelete!!
                        scope.launch {
                            productRepository.deleteProduct(prod.id)
                            LogManager.registrarLog(context, "eliminacion", userName) // Log

                            try {
                                withContext(Dispatchers.IO) {
                                    RetrofitClient.instance.deleteProduct(prod.id)
                                }
                            } catch (e: Exception) { }

                            Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                            showDeleteDialog = false
                            productToDelete = null
                        }
                    }
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// ... Las funciones auxiliares (SummaryCard, ProductItem, EmptyState) se mantienen igual que en el código anterior ...
@Composable
fun SummaryCard(count: Int, total: Double, format: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total Inventario", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    text = format.format(total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(modifier = Modifier.height(40.dp).width(1.dp), color = Color.LightGray) // Corrección Divider
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Productos", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUri.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(product.imageUri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.descripcion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(dateFormat.format(Date(product.fechaFabricacion)), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currencyFormat.format(product.costo), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = if (product.disponibilidad) Color(0xFFE8F5E9) else Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp)) {
                        Text(if (product.disponibilidad) "Stock" else "Agotado", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if (product.disponibilidad) Color(0xFF2E7D32) else Color(0xFFC62828))
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No tienes productos aún", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
    }
}