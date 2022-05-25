package com.example.iadcoins

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil.compose.rememberImagePainter
import com.android.volley.toolbox.JsonObjectRequest
import com.example.iadcoins.ui.theme.IadCoinsTheme
import com.android.volley.*
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack

lateinit var CACHE: DiskBasedCache
lateinit var NETWORK : BasicNetwork
lateinit var REQUEST_QUEUE : RequestQueue

var COIN_NAME : String = ""
var COIN_IMAGE : String = ""
var EXCHANGE_RATE : Double = 0.0

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CACHE = DiskBasedCache(cacheDir, 1024 * 1024) // 1MB cap
        NETWORK = BasicNetwork(HurlStack())
        REQUEST_QUEUE = RequestQueue(CACHE, NETWORK).apply {
            start()
        }

        setContent {
            IadCoinsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    requestInformation("Android")
                    coinInput()
                }
            }
        }
    }
}

@Composable
fun coinInput() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        var text by remember { mutableStateOf("") }
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                COIN_NAME = text
                requestInformation(COIN_NAME)
            },
            label = {
                Text(text = "Название криптовалюты")
            }
        )
        Text(
            text = "Курс: $EXCHANGE_RATE"
        )
        val painter = rememberImagePainter(
            data = COIN_IMAGE
        )
        Image(
            painter = painter,
            contentDescription = "Иконка криптовалюты"
        )
    }
}

fun requestInformation(coinName : String) {
    val requestUrl = "https://api.coingecko.com/api/v3/coins/$coinName"
    val jsonObjectRequest = JsonObjectRequest (
        Request.Method.GET, requestUrl, null,
        { response ->
            println("Response: %s".format(response.toString()))

            COIN_NAME = response.getString("name")
            EXCHANGE_RATE = response.getJSONObject("market_data").getJSONObject("current_price").getString("usd").toDoubleOrNull()!!
            COIN_IMAGE = response.getJSONObject("image").getString("large")
        },
        { error ->
            COIN_NAME = ""
            EXCHANGE_RATE = 0.0
            COIN_IMAGE = ""
        }
    )
    REQUEST_QUEUE.add(jsonObjectRequest)
}
