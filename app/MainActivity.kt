package cl.kaiju.stock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.kaiju.stock.navigation.AppNavHost

// Solo llama a AppNavHost(): toda la app cuelga de ahí.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppNavHost() }
    }
}
