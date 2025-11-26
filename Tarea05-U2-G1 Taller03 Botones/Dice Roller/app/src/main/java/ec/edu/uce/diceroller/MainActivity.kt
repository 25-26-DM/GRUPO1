package ec.edu.uce.diceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ec.edu.uce.diceroller.ui.theme.DiceRollerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiceRollerTheme {
                DiceWithButtonAndImage(modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun DiceWithButtonAndImage(modifier: Modifier = Modifier) {
    var result by remember { mutableIntStateOf(1) }
    val imageResource = when (result) {
        1 -> R.drawable.dice_1
        2 -> R.drawable.dice_2
        3 -> R.drawable.dice_3
        4 -> R.drawable.dice_4
        5 -> R.drawable.dice_5
        else -> R.drawable.dice_6
    }
    val numberAsText = when (result) {
        1 -> stringResource(R.string.dice_one)
        2 -> stringResource(R.string.dice_two)
        3 -> stringResource(R.string.dice_three)
        4 -> stringResource(R.string.dice_four)
        5 -> stringResource(R.string.dice_five)
        else -> stringResource(R.string.dice_six)
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = numberAsText,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = painterResource(id = imageResource),
            contentDescription = result.toString()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { result = (1..6).random() },
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 20.dp)
        ) {
            Text(
                stringResource(R.string.roll),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DiceRollerPreview() {
    DiceRollerTheme {
        DiceWithButtonAndImage(modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center))
    }
}
