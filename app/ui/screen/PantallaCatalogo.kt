package cl.kaiju.stock.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.kaiju.stock.model.Usuario
import cl.kaiju.stock.model.categoriasRopa
import cl.kaiju.stock.ui.components.TarjetaProducto
import cl.kaiju.stock.ui.theme.AmbarStock
import cl.kaiju.stock.viewmodel.InventarioViewModel

@Composable
fun PantallaCatalogo(
    vm: InventarioViewModel,
    usuario: Usuario,
    onDetalle: (String) -> Unit,
    onNuevo: () -> Unit,
    onSalir: () -> Unit
) {
    val productos by vm.productosFiltrados.collectAsState(initial = emptyList())
    val alertas by vm.alertas.collectAsState(initial = emptyList())
    val busqueda by vm.busqueda.collectAsState()
    val categoria by vm.categoriaFiltro.collectAsState()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Catálogo", style = MaterialTheme.typography.headlineSmall)
                    Text("${usuario.nombre} · ${usuario.rol.etiqueta}", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onSalir) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión") }
            }

            // Animación con propósito: el aviso aparece y desaparece suavemente según haya alertas.
            AnimatedVisibility(visible = alertas.isNotEmpty() && usuario.rol.veAlertas) {
                Card(colors = CardDefaults.cardColors(containerColor = AmbarStock.copy(alpha = 0.15f)), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmbarStock)
                        Spacer(Modifier.width(8.dp))
                        Text("${alertas.size} productos con stock bajo")
                    }
                }
            }

            OutlinedTextField(
                value = busqueda,
                onValueChange = vm::onBusquedaChange,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = { if (busqueda.isNotEmpty()) IconButton(onClick = { vm.onBusquedaChange("") }) { Icon(Icons.Default.Clear, contentDescription = "Borrar búsqueda") } },
                placeholder = { Text("Buscar por nombre o código") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                item { FilterChip(selected = categoria == null, onClick = { vm.onCategoriaFiltro(null) }, label = { Text("Todas") }) }
                items(categoriasRopa) { cat ->
                    FilterChip(selected = categoria == cat, onClick = { vm.onCategoriaFiltro(cat) }, label = { Text(cat) })
                }
            }

            if (productos.isEmpty()) {
                Text("No hay productos que coincidan", style = MaterialTheme.typography.bodyMedium)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                items(productos, key = { it.codigo }) { p -> TarjetaProducto(p) { onDetalle(p.codigo) } }
            }
        }

        // RF-05: solo el administrador ve el botón para crear productos.
        if (usuario.rol.puedeGestionarProductos) {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo producto") },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            )
        }
    }
}
