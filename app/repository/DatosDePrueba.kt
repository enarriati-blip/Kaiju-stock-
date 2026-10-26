package cl.kaiju.stock.repository

import cl.kaiju.stock.model.*
import java.time.LocalDateTime

// Todo ficticio. Hay productos en stock bajo a propósito (KJ-002, KJ-003, KJ-006, KJ-009) para probar alertas.
object DatosDePrueba {

    val usuarios = listOf(
        Usuario(1, "Admin Demo", Rol.ADMINISTRADOR),
        Usuario(2, "Vendedor Demo", Rol.VENDEDOR),
        Usuario(3, "Encargado Demo", Rol.ENCARGADO_INVENTARIO)
    )

    val productos = listOf(
        Producto("KJ-001", "Polera Kaiju Negra", "Algodón 100%, estampado frontal", "Poleras", "Ropa", 12990, 25, 5, "M · Negro"),
        Producto("KJ-002", "Polerón Godzilla Gris", "Polerón con capucha", "Polerones", "Ropa", 29990, 3, 5, "L · Gris"),
        Producto("KJ-003", "Jeans Slim Azul", "Corte slim, tiro medio", "Pantalones", "Ropa", 24990, 0, 4, "32 · Azul"),
        Producto("KJ-004", "Gorro Monstruo", "Gorro de lana", "Accesorios", "Accesorio", 6990, 40, 10),
        Producto("KJ-005", "Zapatillas Urban Blancas", "Suela de goma", "Zapatillas", "Calzado", 39990, 12, 3, "42 · Blanco"),
        Producto("KJ-006", "Polera Mothra Verde", "Edición limitada", "Poleras", "Ropa", 13990, 6, 6, "S · Verde"),
        Producto("KJ-007", "Pantalón Cargo Verde", "Seis bolsillos", "Pantalones", "Ropa", 27990, 18, 5, "30 · Verde"),
        Producto("KJ-008", "Mochila Kaiju", "Mochila urbana 20 L", "Accesorios", "Accesorio", 21990, 9, 4),
        Producto("KJ-009", "Polerón Canguro Negro", "Con bolsillo frontal", "Polerones", "Ropa", 31990, 2, 4, "M · Negro"),
        Producto("KJ-010", "Zapatillas Runner Rojas", "Livianas para correr", "Zapatillas", "Calzado", 42990, 15, 5, "41 · Rojo"),
        Producto("KJ-011", "Calcetines Pack 3", "Pack de tres pares", "Accesorios", "Accesorio", 5990, 30, 8)
    )

    val movimientos = listOf(
        Movimiento(1, "KJ-001", TipoMovimiento.ENTRADA, 30, "Llegada de mercadería", LocalDateTime.of(2026, 9, 20, 9, 15), "Encargado Demo"),
        Movimiento(2, "KJ-001", TipoMovimiento.SALIDA_VENTA, 5, "", LocalDateTime.of(2026, 9, 24, 17, 40), "Vendedor Demo"),
        Movimiento(3, "KJ-002", TipoMovimiento.ENTRADA, 10, "Llegada de mercadería", LocalDateTime.of(2026, 9, 18, 10, 0), "Encargado Demo"),
        Movimiento(4, "KJ-002", TipoMovimiento.SALIDA_VENTA, 7, "", LocalDateTime.of(2026, 9, 27, 12, 20), "Vendedor Demo"),
        Movimiento(5, "KJ-003", TipoMovimiento.SALIDA_VENTA, 4, "", LocalDateTime.of(2026, 9, 29, 16, 5), "Vendedor Demo"),
        Movimiento(6, "KJ-003", TipoMovimiento.AJUSTE, -2, "Prendas dañadas en bodega", LocalDateTime.of(2026, 9, 30, 11, 30), "Encargado Demo")
    )
}
