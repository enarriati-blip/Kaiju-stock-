package cl.kaiju.stock.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector

// Las 6 rutas de la app. "ruta" es el patrón que se registra en el NavHost;
// "destino" es el texto real al que se navega (sin los {huecos}).
sealed class Rutas(val ruta: String, val titulo: String, val icono: ImageVector) {

    open val destino: String get() = ruta

    object Login : Rutas("login", "Ingreso", Icons.Default.Person)

    object Catalogo : Rutas("catalogo", "Catálogo", Icons.Default.Inventory)

    // Argumento obligatorio: detalle/KJ-001
    object Detalle : Rutas("detalle/{codigo}", "Detalle", Icons.Default.Info) {
        fun conCodigo(codigo: String) = "detalle/$codigo"
    }

    // Argumento opcional: sin código = crear; con código = editar.
    object FormularioProducto : Rutas("formulario_producto?codigo={codigo}", "Producto", Icons.Default.Edit) {
        fun conCodigo(codigo: String?) = if (codigo == null) "formulario_producto" else "formulario_producto?codigo=$codigo"
        override val destino get() = "formulario_producto"
    }

    // Argumento opcional: con código llega con el producto ya elegido (desde el detalle).
    object Movimiento : Rutas("movimiento?codigo={codigo}", "Movimiento", Icons.Default.SwapVert) {
        fun conCodigo(codigo: String?) = if (codigo == null) "movimiento" else "movimiento?codigo=$codigo"
        override val destino get() = "movimiento"
    }

    object Alertas : Rutas("alertas", "Alertas", Icons.Default.NotificationsActive)

    companion object {
        // Las 3 pantallas del menú inferior.
        val menu: List<Rutas> by lazy { listOf(Catalogo, Movimiento, Alertas) }
    }
}
