package com.example.happybirthday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.happybirthday.ui.theme.HappyBirthdayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HappyBirthdayTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingImage(
                        message = stringResource(R.string.happy_birthday_text),
                        from = stringResource(R.string.signature_text),
                        date = "Febrero 26",
                        location = "GoogleMaps",
                        email = "invitacion@android.com",
                        phone = "+1 (800) 123-4567"
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingImage(
    message: String,
    from: String,
    date: String,
    location: String,
    email: String,
    phone: String,
    modifier: Modifier = Modifier
) {
    val image = painterResource(R.drawable.androidparty1)
    Box(modifier) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.4F
        )
        GreetingText(
            message = message,
            from = from,
            date = date,
            location = location,
            email = email,
            phone = phone,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Composable
fun GreetingText(
    message: String,
    from: String,
    date: String,
    location: String,
    email: String,
    phone: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = message,
            fontSize = 50.sp,
            lineHeight = 60.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 64.dp)
        )
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "Date")
                Text(text = date, modifier = Modifier.padding(18.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, contentDescription = "Location")
                Text(text = location, modifier = Modifier.padding(18.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = "Email")
                Text(text = email, modifier = Modifier.padding(18.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Call, contentDescription = "Phone")
                Text(text = phone, modifier = Modifier.padding(18.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = "Share")
                Text(
                    text = stringResource(R.string.share_with_others),
                    modifier = Modifier.padding(18.dp)
                )
            }
            Text(
                text = from,
                fontSize = 30.sp,
                modifier = Modifier
                    .padding(top = 25.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BirthdayCardPreview() {
    HappyBirthdayTheme {
        GreetingImage(
            message = stringResource(R.string.happy_birthday_text),
            from = stringResource(R.string.signature_text),
            date = "Febrero 26",
            location = "GoogleMaps",
            email = "invitacion@android.com",
            phone = "+1 (800) 123-4567"
        )
    }
}
