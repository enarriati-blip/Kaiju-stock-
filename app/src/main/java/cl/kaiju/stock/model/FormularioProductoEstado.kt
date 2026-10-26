package cl.kaiju.stock.model

// Lo que el usuario escribe. Todo es String: los números se convierten al validar.
data class FormularioProductoEstado(
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val precio: String = "",
    val stockMinimo: String = "",
    val detalle: String = "",
    val esEdicion: Boolean = false
)

// Un mensaje por campo (null = sin error).
data class ErroresProducto(
    val codigo: String? = null,
    val nombre: String? = null,
    val categoria: String? = null,
    val precio: String? = null,
    val stockMinimo: String? = null
) {
    val hayErrores: Boolean
        get() = listOfNotNull(codigo, nombre, categoria, precio, stockMinimo).isNotEmpty()
}
