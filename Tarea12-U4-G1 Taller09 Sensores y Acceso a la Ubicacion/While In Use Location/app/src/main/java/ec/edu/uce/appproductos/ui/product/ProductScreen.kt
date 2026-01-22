package ec.edu.uce.appproductos.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ec.edu.uce.appproductos.data.ProductRepository
import ec.edu.uce.appproductos.model.Product
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productId: Int?,
    productRepository: ProductRepository,
    onSave: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var descripcion by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var disponibilidad by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(productId != null) }

    LaunchedEffect(productId) {
        if (productId != null) {
            val product = productRepository.getProducts().find { it.id == productId }
            product?.let {
                descripcion = it.descripcion
                costo = it.costo.toString()
                disponibilidad = it.disponibilidad
            }
            isLoading = false
        }
    }

    val isFormValid by derivedStateOf {
        descripcion.isNotBlank() && costo.isNotBlank() && costo.toDoubleOrNull() != null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId == null) "Nuevo Producto" else "Editar Producto") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = costo,
                    onValueChange = { costo = it },
                    label = { Text("Costo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Text("$") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = disponibilidad,
                        onCheckedChange = { disponibilidad = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disponible")
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val finalCost = costo.toDoubleOrNull() ?: 0.0
                        coroutineScope.launch {
                            val newProduct = Product(
                                id = productId ?: 0, // 0 para autogenerar en Insert
                                descripcion = descripcion,
                                fechaFabricacion = Date(),
                                costo = finalCost,
                                disponibilidad = disponibilidad
                            )
                            if (productId == null) {
                                productRepository.addProduct(newProduct)
                            } else {
                                productRepository.updateProduct(newProduct)
                            }
                            onSave()
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (productId == null) "Agregar Producto" else "Guardar Cambios")
                }
            }
        }
    }
}
