package cl.kaiju.stock.database

import androidx.room.TypeConverter
import cl.kaiju.stock.model.TipoMovimiento
import java.time.LocalDateTime

// SQLite solo guarda números y texto. Estos convertidores le enseñan a Room a guardar
// los tipos propios (fecha y enum) como texto, y a reconstruirlos al leer.
class Convertidores {

    @TypeConverter
    fun fechaATexto(fecha: LocalDateTime): String = fecha.toString()

    @TypeConverter
    fun textoAFecha(texto: String): LocalDateTime = LocalDateTime.parse(texto)

    @TypeConverter
    fun tipoATexto(tipo: TipoMovimiento): String = tipo.name

    @TypeConverter
    fun textoATipo(texto: String): TipoMovimiento = TipoMovimiento.valueOf(texto)
}
