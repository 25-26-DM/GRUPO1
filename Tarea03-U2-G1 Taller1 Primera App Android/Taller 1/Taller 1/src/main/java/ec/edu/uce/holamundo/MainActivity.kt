package ec.edu.uce.holamundo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ec.edu.uce.holamundo.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                val deepBlue = Color(0,70,113)
                Surface(modifier = Modifier.fillMaxSize(), color = deepBlue) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Greeting()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting( modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hola mundo",
            color = Color(226,12,55),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Dispositivos moviles UCE",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = "Grupo 1\nAndino Jhon\nBorja Diego\nCajamarca Anthony\nCruz Kevin\nJami Mateo\nQuiguango Wulfer\nValle Armando",
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        val deepBlue = Color(0,70,113)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = deepBlue) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Greeting()
            }
        }
    }
}
