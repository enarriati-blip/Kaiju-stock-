package cl.kaiju.stock.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import cl.kaiju.stock.model.Movimiento
import cl.kaiju.stock.model.Producto
import kotlinx.coroutines.flow.Flow

// Un solo DAO para el inventario porque registrar un movimiento toca DOS tablas a la vez
// (inserta el movimiento y cambia el stock del producto) y debe ser una sola operación.
// Es abstract class (y no interface) para poder tener un método @Transaction con cuerpo.
//
// No hay @Delete a propósito: ni productos ni movimientos se borran en la base (RN-05).
@Dao
abstract class InventarioDao {

    // ---------- Productos ----------

    // Flow: Room vuelve a emitir la lista SOLO cuando la tabla cambia.
    @Query("SELECT * FROM productos ORDER BY nombre")
    abstract fun obtenerProductos(): Flow<List<Producto>>

    // @Upsert: inserta si no existe y actualiza si existe. Se usa en vez de REPLACE porque REPLACE
    // borra la fila y volvería a crearla, lo que chocaría con la ForeignKey de los movimientos.
    @Upsert
    abstract suspend fun guardarProducto(producto: Producto)

    @Insert
    abstract suspend fun insertarProductos(productos: List<Producto>)

    @Query("SELECT COUNT(*) FROM productos")
    abstract suspend fun contarProductos(): Int

    // ---------- Movimientos ----------

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    abstract fun obtenerMovimientos(): Flow<List<Movimiento>>

    @Insert
    abstract suspend fun insertarMovimiento(movimiento: Movimiento)

    @Insert
    abstract suspend fun insertarMovimientos(movimientos: List<Movimiento>)

    // Guarda la ruta de la foto tomada con la cámara.
    @Query("UPDATE productos SET imagenRuta = :ruta WHERE codigo = :codigo")
    abstract suspend fun actualizarImagen(codigo: String, ruta: String)

    // Actualización RELATIVA (stock = stock + efecto) en vez de "poner este número":
    // la suma la hace SQLite sobre el valor real, así dos registros seguidos no se pisan.
    @Query("UPDATE productos SET stockActual = stockActual + :efecto WHERE codigo = :codigo")
    abstract suspend fun sumarStock(codigo: String, efecto: Int)

    // @Transaction: o se guardan las dos cosas (movimiento y nuevo stock) o no se guarda ninguna.
    @Transaction
    open suspend fun registrarMovimiento(movimiento: Movimiento) {
        insertarMovimiento(movimiento)
        sumarStock(movimiento.codigoProducto, movimiento.efecto)
    }
}
