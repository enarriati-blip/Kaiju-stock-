package cl.kaiju.stock.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.kaiju.stock.ui.components.TarjetaProducto
import cl.kaiju.stock.viewmodel.InventarioViewModel

@Composable
fun PantallaAlertas(vm: InventarioViewModel, onDetalle: (String) -> Unit) {
    val alertas by vm.alertas.collectAsState(initial = emptyList())

    Column(Modifier.padding(horizontal = 16.dp)) {
        Text("Alertas de stock", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 12.dp))
        if (alertas.isEmpty()) {
            Text("Todo en orden: no hay productos con stock bajo.")
        } else {
            Text("${alertas.size} productos necesitan reposición", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alertas, key = { it.codigo }) { p -> TarjetaProducto(p) { onDetalle(p.codigo) } }
            }
        }
    }
}
