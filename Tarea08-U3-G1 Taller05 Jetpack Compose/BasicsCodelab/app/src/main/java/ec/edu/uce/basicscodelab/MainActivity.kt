package ec.edu.uce.basicscodelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ec.edu.uce.basicscodelab.ui.theme.BasicsCodelabTheme

// Estructura de datos para un integrante
data class Member(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val address: String,
    val phone: String
)

// Lista con los datos de los integrantes del grupo
private val members = listOf(
    Member(firstName = "John", lastName = "Andino", birthDate = "19/12/2000", address = "Guamani", phone = "0959510327"),
    Member(firstName = "Diego", lastName = "Borja", birthDate = "10/02/1998", address = "Las Casas", phone = "0995711134"),
    Member(firstName = "Anthony", lastName = "Cajamarca", birthDate = "26/07/1999", address = "Ajavi", phone = "0963576870"),
    Member(firstName = "Kevin", lastName = "Cruz", birthDate = "01/04/1999", address = "La Forestal", phone = "0983187384"),
    Member(firstName = "Mateo", lastName = "Jami", birthDate = "27/03/2002", address = "Pedro José Davalos", phone = "0987775446"),
    Member(firstName = "Wulfer", lastName = "Quiguango", birthDate = "No disponible", address = "No disponible", phone = "No disponible"),
    Member(firstName = "Armando", lastName = "Valle", birthDate = "21/07/2000", address = "San Bartolo", phone = "0982613168"),
    Member("Perico", lastName = "Palotes", birthDate = "No disponible", address = "No disponible", phone = "No disponible")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicsCodelabTheme {
                MyApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MyApp(modifier: Modifier = Modifier) {
    var shouldShowOnboarding by remember { mutableStateOf(true) }

    Surface(modifier, color = MaterialTheme.colorScheme.background) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = { shouldShowOnboarding = false })
        } else {
            Greetings()
        }
    }
}

@Composable
fun OnboardingScreen(onContinueClicked: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Bienvenido!",
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Descubre la información del equipo.",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(32.dp))
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = onContinueClicked
        ) {
            Text("Continuar")
        }
    }
}

@Composable
private fun Greetings(
    modifier: Modifier = Modifier,
    memberList: List<Member> = members
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = memberList) { member ->
            MemberCard(member = member)
        }
    }
}

@Composable
private fun MemberCard(member: Member) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${member.firstName} ${member.lastName}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Mostrar menos" else "Mostrar más",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                MemberDetailRow(icon = Icons.Filled.Cake, text = "Fecha de Nacimiento: ${member.birthDate}")
                MemberDetailRow(icon = Icons.Filled.Home, text = "Dirección: ${member.address}")
                MemberDetailRow(icon = Icons.Filled.Phone, text = "Teléfono: ${member.phone}")
            }
        }
    }
}

@Composable
private fun MemberDetailRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(end = 16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun DefaultPreview() {
    BasicsCodelabTheme {
        Greetings()
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    BasicsCodelabTheme {
        OnboardingScreen(onContinueClicked = {})
    }
}

@Preview
@Composable
fun MyAppPreview() {
    BasicsCodelabTheme {
        MyApp(Modifier.fillMaxSize())
    }
}
