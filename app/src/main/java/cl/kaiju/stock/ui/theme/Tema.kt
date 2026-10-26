package cl.kaiju.stock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// IDENTIDAD VISUAL: cambien estos colores por los de su equipo y justifiquen la paleta en el README.
// Colores con buen contraste, pensados para leer en bodega.
val AzulNoche = Color(0xFF263238)
val NaranjaKaiju = Color(0xFFD84315)
val VerdeStock = Color(0xFF2E7D32)
val AmbarStock = Color(0xFFEF6C00)
val RojoStock = Color(0xFFC62828)

private val Colores = lightColorScheme(
    primary = AzulNoche,
    secondary = NaranjaKaiju,
    tertiary = VerdeStock,
    error = RojoStock
)

@Composable
fun KaijuTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colores, content = content)
}
