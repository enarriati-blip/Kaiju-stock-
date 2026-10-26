package cl.kaiju.stock.model

data class FormularioMovimientoEstado(
    val codigoProducto: String = "",
    val tipo: TipoMovimiento? = null,
    val cantidad: String = "",
    val motivo: String = "",
    val ajusteAumenta: Boolean = true      // solo se usa cuando el tipo es AJUSTE
)

data class ErroresMovimiento(
    val producto: String? = null,
    val tipo: String? = null,
    val cantidad: String? = null,
    val motivo: String? = null
) {
    val hayErrores: Boolean
        get() = listOfNotNull(producto, tipo, cantidad, motivo).isNotEmpty()
}
