package cl.kaiju.stock.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.kaiju.stock.model.Movimiento
import cl.kaiju.stock.model.Usuario
import cl.kaiju.stock.nativo.GestorFotos
import cl.kaiju.stock.ui.components.IndicadorStock
import cl.kaiju.stock.ui.components.comoPesos
import cl.kaiju.stock.ui.theme.RojoStock
import cl.kaiju.stock.ui.theme.VerdeStock
import cl.kaiju.stock.viewmodel.InventarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.format.DateTimeFormatter

private val formatoFecha = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

@Composable
fun PantallaDetalleProducto(
    vm: InventarioViewModel,
    usuario: Usuario,
    codigo: String,
    onEditar: (String) -> Unit,
    onMovimiento: (String) -> Unit
) {
    val contexto = LocalContext.current
    val productos by vm.productos.collectAsState()
    val producto = productos.find { it.codigo == codigo }
    // remember(codigo): crea el Flow una sola vez; si no, se reiniciaría en cada recomposición.
    val historial by remember(codigo) { vm.historialDe(codigo) }.collectAsState(initial = emptyList())

    // ---------- RECURSO NATIVO: CÁMARA ----------
    // Ruta del archivo donde se está tomando la foto. rememberSaveable: si se rota la pantalla mientras
    // la cámara está abierta, la ruta no se pierde.
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var permisoDenegado by remember { mutableStateOf(false) }

    // 3) La cámara devuelve si la foto se tomó. Si sí, el ViewModel guarda la ruta; si no, se borra el archivo vacío.
    val lanzadorCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaPendiente
        if (ruta != null) {
            if (exito) vm.guardarFoto(codigo, ruta) else File(ruta).delete()
        }
        rutaPendiente = null
    }

    // 2) Crea el archivo en la carpeta privada de la app y abre la cámara con un URI temporal (FileProvider).
    fun abrirCamara() {
        val archivo = GestorFotos.crearArchivo(contexto, codigo)
        rutaPendiente = archivo.absolutePath
        lanzadorCamara.launch(GestorFotos.uriDe(contexto, archivo))
    }

    // 1) Permiso CAMERA en tiempo de ejecución. Si el usuario lo niega, la app sigue funcionando sin la foto.
    val lanzadorPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        permisoDenegado = !concedido
        if (concedido) abrirCamara()
    }

    if (producto == null) {
        Text("Producto no encontrado", modifier = Modifier.padding(16.dp))
        return
    }

    // La foto se lee en un hilo de fondo (IO) para no congelar la pantalla; produceState entrega el resultado.
    val foto by produceState<Bitmap?>(initialValue = null, producto.imagenRuta) {
        value = withContext(Dispatchers.IO) { GestorFotos.cargarBitmap(producto.imagenRuta) }
    }

    LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Spacer(Modifier.height(8.dp))
            foto?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de ${producto.nombre}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(producto.nombre, style = MaterialTheme.typography.headlineSmall)
            Text("${producto.codigo} · ${producto.categoria} · ${producto.tipo}", style = MaterialTheme.typography.bodySmall)
            if (producto.descripcion.isNotBlank()) Text(producto.descripcion)
            if (producto.detalle.isNotBlank()) Text("Detalle: ${producto.detalle}")
            Text("Precio: ${producto.precio.comoPesos()}", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Stock: ${producto.stockActual} (mínimo ${producto.stockMinimo})")
                IndicadorStock(producto)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onMovimiento(producto.codigo) }) { Text("Registrar movimiento") }
                if (usuario.rol.puedeGestionarProductos) {
                    OutlinedButton(onClick = { onEditar(producto.codigo) }) { Text("Editar") }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                val concedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (concedido) abrirCamara() else lanzadorPermiso.launch(Manifest.permission.CAMERA)
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (producto.imagenRuta.isBlank()) "Tomar foto del producto" else "Cambiar foto")
            }
            if (permisoDenegado) {
                Text(
                    "Sin el permiso de cámara no se puede tomar la foto. Puedes activarlo en Ajustes > Aplicaciones > Kaiju Stock > Permisos.",
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Historial de movimientos", style = MaterialTheme.typography.titleMedium)
        }
        if (historial.isEmpty()) item { Text("Aún no hay movimientos") }
        items(historial, key = { it.id }) { FilaMovimiento(it) }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun FilaMovimiento(m: Movimiento) {
    val color = if (m.efecto >= 0) VerdeStock else RojoStock
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(m.tipo.etiqueta, style = MaterialTheme.typography.titleSmall)
                Text("${m.fecha.format(formatoFecha)} · ${m.usuario}", style = MaterialTheme.typography.bodySmall)
                if (m.motivo.isNotBlank()) Text("Motivo: ${m.motivo}", style = MaterialTheme.typography.bodySmall)
            }
            Text(if (m.efecto > 0) "+${m.efecto}" else "${m.efecto}", color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}
