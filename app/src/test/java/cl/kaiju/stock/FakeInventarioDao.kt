package cl.kaiju.stock

import cl.kaiju.stock.database.InventarioDao
import cl.kaiju.stock.model.Movimiento
import cl.kaiju.stock.model.Producto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

// DAO falso: cumple el mismo contrato que el DAO real pero guarda en listas en memoria.
// Así se prueba el ViewModel sin emulador y sin Room.
class FakeInventarioDao : InventarioDao() {

    private val productos = MutableStateFlow<List<Producto>>(emptyList())
    private val movimientos = MutableStateFlow<List<Movimiento>>(emptyList())
    private var siguienteId = 1

    override fun obtenerProductos(): Flow<List<Producto>> = productos

    override suspend fun guardarProducto(producto: Producto) {
        productos.update { lista ->
            if (lista.any { it.codigo == producto.codigo }) lista.map { if (it.codigo == producto.codigo) producto else it }
            else lista + producto
        }
    }

    override suspend fun insertarProductos(productos: List<Producto>) {
        this.productos.update { it + productos }
    }

    override suspend fun contarProductos(): Int = productos.value.size

    override fun obtenerMovimientos(): Flow<List<Movimiento>> = movimientos

    override suspend fun insertarMovimiento(movimiento: Movimiento) {
        movimientos.update { it + movimiento.copy(id = siguienteId++) }
    }

    override suspend fun insertarMovimientos(movimientos: List<Movimiento>) {
        this.movimientos.update { it + movimientos }
        siguienteId = (this.movimientos.value.maxOfOrNull { it.id } ?: 0) + 1
    }

    override suspend fun actualizarImagen(codigo: String, ruta: String) {
        productos.update { lista -> lista.map { if (it.codigo == codigo) it.copy(imagenRuta = ruta) else it } }
    }

    override suspend fun sumarStock(codigo: String, efecto: Int) {
        productos.update { lista ->
            lista.map { if (it.codigo == codigo) it.copy(stockActual = it.stockActual + efecto) else it }
        }
    }
}
