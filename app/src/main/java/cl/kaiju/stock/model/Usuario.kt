package cl.kaiju.stock.model

enum class Rol(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    VENDEDOR("Personal de venta"),
    ENCARGADO_INVENTARIO("Encargado de inventario");

    // Qué puede hacer cada perfil (RF-01). Si el cliente cambia un permiso, se cambia solo aquí.
    val puedeGestionarProductos: Boolean get() = this == ADMINISTRADOR   // RF-05
    val veAlertas: Boolean get() = this != VENDEDOR                       // RF-09
    val tiposPermitidos: List<TipoMovimiento>                             // RF-06, RF-07, RF-08
        get() = if (this == VENDEDOR) listOf(TipoMovimiento.SALIDA_VENTA) else TipoMovimiento.values().toList()
}

data class Usuario(val id: Int, val nombre: String, val rol: Rol)
