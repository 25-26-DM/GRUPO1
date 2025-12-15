package ec.edu.uce.appproductos.ui.passwordhash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ec.edu.uce.appproductos.data.hashString

@Composable
fun PasswordHashScreen(
    userName: String,
    password: String,
    onContinueToHome: (String) -> Unit
) {
    val hashedPassword = hashString(password)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("¡Inicio de sesión exitoso!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Contraseña: $password")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Hash SHA-256: $hashedPassword")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onContinueToHome(userName) }) {
            Text("Continuar a la pantalla de inicio")
        }
    }
}
