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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.kaiju.stock.model.categoriasRopa
import cl.kaiju.stock.ui.components.CampoSelector
import cl.kaiju.stock.ui.components.CampoTexto
import cl.kaiju.stock.viewmodel.InventarioViewModel

// El estado se prepara ANTES de navegar (vm.iniciarFormProducto). Aquí solo se muestra y se avisa.
@Composable
fun PantallaFormularioProducto(vm: InventarioViewModel, onGuardado: () -> Unit) {
    val f by vm.formProducto.collectAsState()
    val e by vm.erroresProducto.collectAsState()

    Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(if (f.esEdicion) "Editar producto" else "Nuevo producto", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        // Al editar, el código no se puede cambiar (es la identidad del producto, RN-03).
        CampoTexto(f.codigo, vm::onCodigo, "Código", e.codigo, Icons.Default.Tag, enabled = !f.esEdicion)
        CampoTexto(f.nombre, vm::onNombre, "Nombre", e.nombre, Icons.Default.ShoppingCart)
        CampoTexto(f.descripcion, vm::onDescripcion, "Descripción (opcional)", null, Icons.Default.Description, unaLinea = false)
        CampoSelector(f.categoria, "Categoría", categoriasRopa, { i -> vm.onCategoria(categoriasRopa[i]) }, e.categoria, Icons.Default.Category)
        CampoTexto(f.precio, vm::onPrecio, "Precio (CLP)", e.precio, Icons.Default.AttachMoney, numerico = true)
        CampoTexto(f.stockMinimo, vm::onStockMinimo, "Stock mínimo", e.stockMinimo, Icons.Default.Inventory, numerico = true)
        CampoTexto(f.detalle, vm::onDetalle, "Talla y color (opcional)", null, Icons.Default.Edit)

        Spacer(Modifier.height(8.dp))
        Button(onClick = { if (vm.guardarProducto()) onGuardado() }, modifier = Modifier.fillMaxWidth()) {
            Text(if (f.esEdicion) "Guardar cambios" else "Crear producto")
        }
    }
}
