package com.grocks.ads.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grocks.ads.GrocksAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var lastResult by remember { mutableStateOf("Нажми кнопку для теста.") }

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "GrocksAds Example",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = lastResult,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 24.dp, bottom = 24.dp),
                        )
                        Button(
                            onClick = {
                                GrocksAds.setApiKey("example-key")
                                GrocksAds.showAd(this@MainActivity) { result ->
                                    lastResult =
                                        result.fold(
                                            onSuccess = { "Успех! Реклама закрыта!" },
                                            onFailure = { e -> "Ошибка: ${e.message ?: e.toString()}" },
                                        )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Показать рекламу")
                        }
                    }
                }
            }
        }
    }
}
