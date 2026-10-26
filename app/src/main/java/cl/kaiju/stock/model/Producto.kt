package cl.kaiju.stock.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity convierte esta clase en la tabla "productos". Cada Producto es una fila.
// La clave primaria es el propio código (ya es único por RN-03), así que no hace falta un id aparte.
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val descripcion: String,
    val categoria: String,
    val tipo: String,
    val precio: Int,
    val stockActual: Int,
    val stockMinimo: Int,
    val detalle: String = "",
    val imagenRuta: String = ""      // ruta de la foto tomada con la cámara (vacío = sin foto)
) {
    // Propiedad calculada (sin campo de respaldo): Room la ignora, no es una columna.
    // RN-06: en alerta cuando la cantidad disponible es igual o menor al stock mínimo.
    val enAlerta: Boolean get() = stockActual <= stockMinimo
}

// Lista fija para el selector de categoría del formulario y para los filtros del catálogo.
val categoriasRopa = listOf("Poleras", "Polerones", "Pantalones", "Zapatillas", "Accesorios")
