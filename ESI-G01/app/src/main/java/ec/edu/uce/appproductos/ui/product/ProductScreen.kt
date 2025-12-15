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
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(product: Product?, onSave: () -> Unit) {
    var descripcion by remember { mutableStateOf(product?.descripcion ?: "") }
    var costo by remember { mutableStateOf(product?.costo?.toString() ?: "") }
    var disponibilidad by remember { mutableStateOf(product?.disponibilidad ?: true) }

    val isFormValid by derivedStateOf {
        descripcion.isNotBlank() && costo.isNotBlank() && costo.toDoubleOrNull() != null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (product == null) "Nuevo Producto" else "Editar Producto") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
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
                    val newProduct = Product(
                        id = product?.id ?: (ProductRepository.getProducts().maxOfOrNull { it.id } ?: 0) + 1,
                        descripcion = descripcion,
                        fechaFabricacion = Date(),
                        costo = finalCost,
                        disponibilidad = disponibilidad
                    )
                    if (product == null) {
                        ProductRepository.addProduct(newProduct)
                    } else {
                        ProductRepository.updateProduct(newProduct)
                    }
                    onSave()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (product == null) "Agregar Producto" else "Guardar Cambios")
            }
        }
    }
}
