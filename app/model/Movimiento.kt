package cl.kaiju.stock.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

enum class TipoMovimiento(val etiqueta: String) {
    ENTRADA("Entrada"),
    SALIDA_VENTA("Salida por venta"),
    SALIDA_CONSUMO("Salida por consumo"),
    AJUSTE("Ajuste")
}

// RN-05: un movimiento nunca se edita ni se borra. Si hubo un error, se corrige con un AJUSTE.
// En AJUSTE, cantidad puede ser negativa (disminuye) o positiva (aumenta).
//
// ForeignKey: la base de datos garantiza que cada movimiento pertenece a un producto que existe,
// y RESTRICT impide borrar un producto que tenga historial (trazabilidad).
// Index: acelera la búsqueda del historial de un producto y evita una advertencia de Room.
@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["codigo"],
            childColumns = ["codigoProducto"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("codigoProducto")]
)
data class Movimiento(
    // autoGenerate: Room asigna el id al insertar. El 0 es solo el valor de partida.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigoProducto: String,
    val tipo: TipoMovimiento,
    val cantidad: Int,
    val motivo: String,
    val fecha: LocalDateTime,
    val usuario: String
) {
    // Cuánto cambia el stock con este movimiento (puede ser negativo). No es columna.
    val efecto: Int
        get() = when (tipo) {
            TipoMovimiento.ENTRADA -> cantidad
            TipoMovimiento.SALIDA_VENTA, TipoMovimiento.SALIDA_CONSUMO -> -cantidad
            TipoMovimiento.AJUSTE -> cantidad
        }
}
