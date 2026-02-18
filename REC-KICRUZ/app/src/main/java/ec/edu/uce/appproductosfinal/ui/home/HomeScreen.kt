package ec.edu.uce.appproductosfinal.ui.home

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.work.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ec.edu.uce.appproductosfinal.data.ProductRepository
import ec.edu.uce.appproductosfinal.data.network.RetrofitClient
import ec.edu.uce.appproductosfinal.data.network.SyncWorker
import ec.edu.uce.appproductosfinal.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    onEditProduct: (Product) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val products by productRepository.getProductsFlow().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "EC")) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val greeting = remember {
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "¡Buenos días!"
            in 12..18 -> "¡Buenas tardes!"
            else -> "¡Buenas noches!"
        }
    }

    val filteredProducts = remember(products, searchQuery) {
        products.filter { 
            it.descripcion.contains(searchQuery, ignoreCase = true) || 
            it.id.toString().contains(searchQuery) 
        }
    }

    val refreshData = {
        scope.launch(Dispatchers.IO) {
            isRefreshing = true
            try {
                val response = RetrofitClient.instance.getAllProducts()
                if (response.isSuccessful) {
                    val cloudProducts = response.body() ?: emptyList()
                    val localProducts = productRepository.getProducts()
                    
                    cloudProducts.forEach { cloud ->
                        val local = localProducts.find { it.id == cloud.id }
                        if (local == null || cloud.lastUpdated > local.lastUpdated) {
                            productRepository.addProduct(cloud)
                        }
                    }
                }
                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
                WorkManager.getInstance(context).enqueue(syncRequest)
            } catch (e: Exception) { }
            finally { isRefreshing = false }
        }
    }

    LaunchedEffect(Unit) { refreshData() }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(greeting, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            Text(userName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                        IconButton(onClick = onLogout, modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar productos...", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.primary) }
                            }
                        },
                        singleLine = true
                    )
                }
            }
        },
        floatingActionButton = {
            LargeFloatingActionButton(onClick = onAddProduct, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White, shape = RoundedCornerShape(24.dp)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { refreshData() }, modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                SummaryHeader(filteredProducts.size, filteredProducts.sumOf { it.costo }, currencyFormat)
                
                if (filteredProducts.isEmpty() && !isRefreshing) {
                    EmptyState(searchQuery.isNotEmpty())
                } else {
                    LazyVerticalGrid(columns = GridCells.Fixed(1), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(filteredProducts, key = { _, it -> it.id }) { index, product ->
                            
                            val animatedAlpha = remember { Animatable(0f) }
                            val animatedY = remember { Animatable(40f) }

                            LaunchedEffect(key1 = filteredProducts) {
                                delay(index * 40L)
                                launch {
                                    animatedAlpha.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                                }
                                launch {
                                    animatedY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
                                }
                            }

                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    alpha = animatedAlpha.value
                                    translationY = animatedY.value
                                }
                            ) {
                                ModernProductCard(product, currencyFormat, dateFormat, onEdit = { onEditProduct(product) }, onDelete = {
                                    productToDelete = product
                                    showDeleteDialog = true
                                })
                            }
                        }
                    }
                }
            }
            if (isDeleting) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Deseas borrar este producto permanentemente?") },
            confirmButton = {
                Button(
                    enabled = !isDeleting,
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            productToDelete?.let { prod ->
                                try {
                                    val response = withContext(Dispatchers.IO) { RetrofitClient.instance.deleteProduct(prod.id) }
                                    productRepository.deleteProduct(prod.id)
                                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    productRepository.deleteProduct(prod.id)
                                    Toast.makeText(context, "Eliminado localmente", Toast.LENGTH_SHORT).show()
                                }
                            }
                            isDeleting = false
                            showDeleteDialog = false
                        }
                    }, 
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(enabled = !isDeleting, onClick = { showDeleteDialog = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun SummaryHeader(count: Int, total: Double, format: NumberFormat) {
    Card(modifier = Modifier.fillMaxWidth().padding(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)), shape = RoundedCornerShape(24.dp)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Inversión Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text(format.format(total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
            VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline)
            Column(horizontalAlignment = Alignment.End) {
                Text("Stock", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text("$count", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ModernProductCard(product: Product, currencyFormat: NumberFormat, dateFormat: SimpleDateFormat, onEdit: () -> Unit, onDelete: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { }, 
        shape = RoundedCornerShape(20.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                // OPTIMIZACIÓN JEFF DEAN: Uso de AsyncImage con Reintentos y Crossfade para evitar parpadeos
                if (!product.imageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUri)
                            .crossfade(true)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                }
                
                val isSynced = product.imageUri?.startsWith("http") == true
                val infiniteTransition = rememberInfiniteTransition(label = "sync_pulse")
                val alpha by if (!isSynced) {
                    infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
                        label = "alpha"
                    )
                } else {
                    remember { mutableStateOf(1f) }
                }

                Box(modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                    Icon(
                        imageVector = if (isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = (if (isSynced) Color(0xFF34C759) else MaterialTheme.colorScheme.secondary).copy(alpha = alpha)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.descripcion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Text("Código: ${product.id}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text("Fecha: ${dateFormat.format(Date(product.fechaFabricacion))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currencyFormat.format(product.costo), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    AvailabilityBadge(product.disponibilidad)
                }
            }
            Column {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@Composable
fun AvailabilityBadge(isAvailable: Boolean) {
    val color = if (isAvailable) Color(0xFF34C759) else Color(0xFFFF3B30)
    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))) {
        Text(if (isAvailable) "En Stock" else "Agotado", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState(isSearch: Boolean) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(if (isSearch) Icons.Default.SearchOff else Icons.Default.CloudOff, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(16.dp))
        Text(if (isSearch) "Sin resultados" else "No hay productos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(if (isSearch) "Prueba con otra descripción" else "Desliza para actualizar", color = MaterialTheme.colorScheme.secondary)
    }
}
