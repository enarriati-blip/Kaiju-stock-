package cl.kaiju.stock.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.kaiju.stock.model.TipoMovimiento
import cl.kaiju.stock.model.Usuario
import cl.kaiju.stock.ui.components.CampoSelector
import cl.kaiju.stock.ui.components.CampoTexto
import cl.kaiju.stock.viewmodel.InventarioViewModel

@Composable
fun PantallaMovimiento(vm: InventarioViewModel, usuario: Usuario, onGuardado: () -> Unit) {
    val f by vm.formMov.collectAsState()
    val e by vm.erroresMov.collectAsState()
    val productos by vm.productos.collectAsState()
    val elegido = productos.find { it.codigo == f.codigoProducto }
    val tipos = usuario.rol.tiposPermitidos          // cada perfil ve solo lo que puede registrar

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Registrar movimiento", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        CampoSelector(
            valorMostrado = elegido?.let { "${it.codigo} · ${it.nombre}" } ?: "",
            etiqueta = "Producto",
            opciones = productos.map { "${it.codigo} · ${it.nombre}" },
            onElegir = { i -> vm.onMovProducto(productos[i].codigo) },
            error = e.producto,
            icono = Icons.Default.ShoppingCart
        )
        if (elegido != null) Text("Stock disponible: ${elegido.stockActual}", style = MaterialTheme.typography.bodySmall)

        CampoSelector(
            valorMostrado = f.tipo?.etiqueta ?: "",
            etiqueta = "Tipo de movimiento",
            opciones = tipos.map { it.etiqueta },
            onElegir = { i -> vm.onMovTipo(tipos[i]) },
            error = e.tipo,
            icono = Icons.Default.SwapVert,
            enabled = tipos.size > 1
        )

        // Un ajuste puede subir o bajar el stock: el usuario elige el sentido.
        if (f.tipo == TipoMovimiento.AJUSTE) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = f.ajusteAumenta, onCheckedChange = vm::onMovAjusteAumenta)
                Spacer(Modifier.width(8.dp))
                Text(if (f.ajusteAumenta) "El ajuste aumenta el stock" else "El ajuste disminuye el stock")
            }
        }

        CampoTexto(f.cantidad, vm::onMovCantidad, "Cantidad", e.cantidad, Icons.Default.Tag, numerico = true)
        CampoTexto(
            f.motivo, vm::onMovMotivo,
            if (f.tipo == TipoMovimiento.AJUSTE) "Motivo (obligatorio)" else "Motivo (opcional)",
            e.motivo, Icons.Default.Description, unaLinea = false
        )

        Spacer(Modifier.height(8.dp))
        Button(onClick = { if (vm.guardarMovimiento(usuario)) onGuardado() }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar")
        }
    }
}
