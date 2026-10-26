package cl.kaiju.stock.repository

import cl.kaiju.stock.database.InventarioDao
import cl.kaiju.stock.model.Movimiento
import cl.kaiju.stock.model.Producto
import kotlinx.coroutines.flow.Flow

// El Repository es el ÚNICO que conoce al DAO. No decide nada: las reglas RN viven en el ViewModel.
// El ViewModel no sabe que existe SQLite: si mañana se cambia Room por otra fuente de datos,
// solo se tocan esta clase y el DAO.
class InventarioRepository(private val dao: InventarioDao) {

    val productos: Flow<List<Producto>> = dao.obtenerProductos()

    val movimientos: Flow<List<Movimiento>> = dao.obtenerMovimientos()

    suspend fun guardarProducto(producto: Producto) = dao.guardarProducto(producto)

    suspend fun guardarImagen(codigo: String, ruta: String) = dao.actualizarImagen(codigo, ruta)

    // Guarda el movimiento y actualiza el stock en una sola transacción (RN-05).
    suspend fun registrarMovimiento(movimiento: Movimiento) = dao.registrarMovimiento(movimiento)

    // Primera vez que se abre la app: carga los datos ficticios. Las veces siguientes no hace nada,
    // porque los datos ya están guardados en el archivo kaiju.db.
    suspend fun sembrarDatosDePrueba() {
        if (dao.contarProductos() == 0) {
            dao.insertarProductos(DatosDePrueba.productos)      // primero productos (la ForeignKey lo exige)
            dao.insertarMovimientos(DatosDePrueba.movimientos)
        }
    }
}
