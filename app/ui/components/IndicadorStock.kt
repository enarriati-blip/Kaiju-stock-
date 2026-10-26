package cl.kaiju.stock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.kaiju.stock.model.Producto
import cl.kaiju.stock.ui.theme.AmbarStock
import cl.kaiju.stock.ui.theme.RojoStock
import cl.kaiju.stock.ui.theme.VerdeStock
import java.util.Locale

// La marca de stock bajo (RF-09). Color + texto: no depender solo del color ayuda a la accesibilidad.
@Composable
fun IndicadorStock(producto: Producto, modifier: Modifier = Modifier) {
    val (color, texto) = when {
        producto.stockActual == 0 -> RojoStock to "Agotado"
        producto.enAlerta -> AmbarStock to "Stock bajo"
        else -> VerdeStock to "Disponible"
    }
    // ANIMACIÓN (propósito: retroalimentación): cuando un movimiento cambia el estado del producto
    // (por ejemplo de Disponible a Stock bajo), el color cambia gradualmente y el usuario nota el cambio.
    val colorAnimado by animateColorAsState(targetValue = color, animationSpec = tween(400), label = "colorIndicador")

    Surface(shape = RoundedCornerShape(50), color = colorAnimado.copy(alpha = 0.15f), modifier = modifier) {
        Text(texto, color = colorAnimado, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

// $12.990 (punto como separador de miles, estilo chileno)
fun Int.comoPesos(): String = "$" + String.format(Locale.US, "%,d", this).replace(',', '.')
