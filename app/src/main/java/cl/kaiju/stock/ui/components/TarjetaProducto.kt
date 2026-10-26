package cl.kaiju.stock.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.kaiju.stock.model.Producto

@Composable
fun TarjetaProducto(producto: Producto, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
                Text("${producto.codigo} · ${producto.categoria}", style = MaterialTheme.typography.bodySmall)
                Text(producto.precio.comoPesos(), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                // ANIMACIÓN (propósito: retroalimentación): al registrar un movimiento, el número de stock
                // hace una transición en vez de cambiar de golpe, así se ve qué producto se modificó.
                AnimatedContent(targetState = producto.stockActual, label = "stockAnimado") { stock ->
                    Text("$stock uds.", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(4.dp))
                IndicadorStock(producto)
            }
        }
    }
}
