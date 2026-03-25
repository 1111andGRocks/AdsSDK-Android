package com.grocks.ads

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color

public class GrocksAdActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    GrocksAds.deliverResult(Result.failure(Exception("Закрыто пользователем")))
                    finish()
                }
            },
        )
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    GRockAdRoot(
                        onComplete = { result ->
                            GrocksAds.deliverResult(result)
                            finish()
                        },
                    )
                }
            }
        }
    }
}
