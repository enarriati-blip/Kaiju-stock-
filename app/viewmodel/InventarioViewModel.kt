package cl.kaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.kaiju.stock.model.*
import cl.kaiju.stock.repository.InventarioRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

// Aquí viven TODAS las reglas de negocio (RN-01 a RN-06). La pantalla solo muestra y avisa.
// Recibe el Repository por constructor: este ViewModel es Kotlin puro (no conoce Android ni Room)
// y se puede probar con un DAO falso, sin emulador. Lo arma InventarioViewModelFactory.
class InventarioViewModel(private val repo: InventarioRepository) : ViewModel() {

    // ---------- Catálogo (RF-02, RF-03) ----------

    // stateIn convierte el Flow de Room en un StateFlow para seguir usando collectAsState().
    // Eagerly: queda activo siempre, así productos.value tiene la lista al día y las validaciones
    // (RN-01, RN-03) pueden leerla al instante.
    val productos: StateFlow<List<Producto>> =
        repo.productos.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Carga los datos de prueba solo la primera vez (si la base está vacía).
        viewModelScope.launch { repo.sembrarDatosDePrueba() }
    }

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _categoriaFiltro = MutableStateFlow<String?>(null)   // null = todas
    val categoriaFiltro: StateFlow<String?> = _categoriaFiltro.asStateFlow()

    // combine: se recalcula solo cuando cambia la lista, el texto o la categoría.
    val productosFiltrados: Flow<List<Producto>> =
        combine(productos, _busqueda, _categoriaFiltro) { lista, texto, categoria ->
            lista.filter { p ->
                (categoria == null || p.categoria == categoria) &&
                    (texto.isBlank() || p.nombre.contains(texto, ignoreCase = true) || p.codigo.contains(texto, ignoreCase = true))
            }
        }

    fun onBusquedaChange(v: String) { _busqueda.value = v }
    fun onCategoriaFiltro(v: String?) { _categoriaFiltro.value = v }

    // ---------- Alertas (RF-09) ----------
    val alertas: Flow<List<Producto>> = productos.map { lista -> lista.filter { it.enAlerta } }

    // ---------- Detalle e historial (RF-04) ----------
    fun historialDe(codigo: String): Flow<List<Movimiento>> =
        repo.movimientos.map { lista -> lista.filter { it.codigoProducto == codigo }.sortedByDescending { it.fecha } }

    // ---------- Mensajes de una sola vez (Snackbar) ----------
    private val _mensajes = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val mensajes: SharedFlow<String> = _mensajes.asSharedFlow()

    // Evento para el recurso nativo "notificaciones": un producto quedó en alerta.
    // El ViewModel solo avisa QUE pasó; mostrar la notificación es trabajo de la capa de Android (nativo/).
    private val _alertasStock = MutableSharedFlow<Producto>(extraBufferCapacity = 1)
    val alertasStock: SharedFlow<Producto> = _alertasStock.asSharedFlow()

    private fun productoPorCodigo(codigo: String): Producto? = productos.value.find { it.codigo == codigo }

    // Foto del producto (recurso nativo: cámara). La pantalla toma la foto; aquí solo se guarda la ruta.
    fun guardarFoto(codigo: String, ruta: String) {
        viewModelScope.launch { repo.guardarImagen(codigo, ruta) }
        _mensajes.tryEmit("Foto guardada")
    }

    // ================= Formulario de producto (RF-05) =================
    private val _formProducto = MutableStateFlow(FormularioProductoEstado())
    val formProducto: StateFlow<FormularioProductoEstado> = _formProducto.asStateFlow()

    private val _erroresProducto = MutableStateFlow(ErroresProducto())
    val erroresProducto: StateFlow<ErroresProducto> = _erroresProducto.asStateFlow()

    // Se llama ANTES de navegar: así rotar la pantalla no reinicia lo que el usuario escribió.
    fun iniciarFormProducto(codigo: String?) {
        val p = codigo?.let { productoPorCodigo(it) }
        _formProducto.value = if (p != null) {
            FormularioProductoEstado(p.codigo, p.nombre, p.descripcion, p.categoria, p.precio.toString(), p.stockMinimo.toString(), p.detalle, esEdicion = true)
        } else FormularioProductoEstado()
        _erroresProducto.value = ErroresProducto()
    }

    // Al escribir en un campo se limpia su error.
    fun onCodigo(v: String) { _formProducto.update { it.copy(codigo = v) }; _erroresProducto.update { it.copy(codigo = null) } }
    fun onNombre(v: String) { _formProducto.update { it.copy(nombre = v) }; _erroresProducto.update { it.copy(nombre = null) } }
    fun onDescripcion(v: String) { _formProducto.update { it.copy(descripcion = v) } }
    fun onCategoria(v: String) { _formProducto.update { it.copy(categoria = v) }; _erroresProducto.update { it.copy(categoria = null) } }
    fun onPrecio(v: String) { _formProducto.update { it.copy(precio = v) }; _erroresProducto.update { it.copy(precio = null) } }
    fun onStockMinimo(v: String) { _formProducto.update { it.copy(stockMinimo = v) }; _erroresProducto.update { it.copy(stockMinimo = null) } }
    fun onDetalle(v: String) { _formProducto.update { it.copy(detalle = v) } }

    fun validarProducto(): Boolean {
        val f = _formProducto.value
        val codigo = f.codigo.trim()
        val precio = f.precio.toIntOrNull()
        val minimo = f.stockMinimo.toIntOrNull()
        val errores = ErroresProducto(
            codigo = when {
                codigo.isEmpty() -> "El código es obligatorio"                                  // RN-03
                !f.esEdicion && existeCodigo(codigo) -> "Ya existe un producto con ese código"  // RN-03
                else -> null
            },
            nombre = if (f.nombre.trim().length < 3) "Mínimo 3 caracteres" else null,
            categoria = if (f.categoria.isBlank()) "Elige una categoría" else null,
            precio = if (precio == null || precio <= 0) "Ingresa un precio entero mayor que 0" else null,       // RN-02
            stockMinimo = if (minimo == null || minimo < 0) "Ingresa un número entero de 0 o más" else null     // RN-02
        )
        _erroresProducto.value = errores
        return !errores.hayErrores
    }

    private fun existeCodigo(codigo: String): Boolean =
        productos.value.any { it.codigo.equals(codigo, ignoreCase = true) }

    // Valida en el momento y devuelve true/false; la escritura en la base corre en una corrutina,
    // fuera del hilo principal. No hace falta tocar "productos": el Flow de Room avisa solo.
    fun guardarProducto(): Boolean {
        if (!validarProducto()) return false
        val f = _formProducto.value
        val codigo = f.codigo.trim()
        val producto = Producto(
            codigo = codigo,
            nombre = f.nombre.trim(),
            descripcion = f.descripcion.trim(),
            categoria = f.categoria,
            tipo = tipoDe(f.categoria),
            precio = f.precio.toInt(),
            // El stock solo cambia con movimientos (RN-05): un producto nuevo parte en 0.
            stockActual = productoPorCodigo(codigo)?.stockActual ?: 0,
            stockMinimo = f.stockMinimo.toInt(),
            detalle = f.detalle.trim(),
            imagenRuta = productoPorCodigo(codigo)?.imagenRuta ?: ""      // al editar no se pierde la foto
        )
        viewModelScope.launch { repo.guardarProducto(producto) }
        _mensajes.tryEmit(if (f.esEdicion) "Producto actualizado" else "Producto creado")
        return true
    }

    private fun tipoDe(categoria: String) = when (categoria) {
        "Zapatillas" -> "Calzado"
        "Accesorios" -> "Accesorio"
        else -> "Ropa"
    }

    // ================= Formulario de movimiento (RF-06, RF-07, RF-08) =================
    private val _formMov = MutableStateFlow(FormularioMovimientoEstado())
    val formMov: StateFlow<FormularioMovimientoEstado> = _formMov.asStateFlow()

    private val _erroresMov = MutableStateFlow(ErroresMovimiento())
    val erroresMov: StateFlow<ErroresMovimiento> = _erroresMov.asStateFlow()

    fun iniciarFormMovimiento(codigo: String?, usuario: Usuario) {
        _formMov.value = FormularioMovimientoEstado(
            codigoProducto = codigo ?: "",
            tipo = usuario.rol.tiposPermitidos.singleOrNull()   // el vendedor solo puede vender: queda elegido
        )
        _erroresMov.value = ErroresMovimiento()
    }

    fun onMovProducto(v: String) { _formMov.update { it.copy(codigoProducto = v) }; _erroresMov.update { it.copy(producto = null, cantidad = null) } }
    fun onMovTipo(v: TipoMovimiento) { _formMov.update { it.copy(tipo = v) }; _erroresMov.update { it.copy(tipo = null, cantidad = null, motivo = null) } }
    fun onMovCantidad(v: String) { _formMov.update { it.copy(cantidad = v) }; _erroresMov.update { it.copy(cantidad = null) } }
    fun onMovMotivo(v: String) { _formMov.update { it.copy(motivo = v) }; _erroresMov.update { it.copy(motivo = null) } }
    fun onMovAjusteAumenta(v: Boolean) { _formMov.update { it.copy(ajusteAumenta = v) }; _erroresMov.update { it.copy(cantidad = null) } }

    fun validarMovimiento(usuario: Usuario): Boolean {
        val f = _formMov.value
        val producto = productoPorCodigo(f.codigoProducto)
        val cantidad = f.cantidad.toIntOrNull()
        val reduceStock = f.tipo == TipoMovimiento.SALIDA_VENTA || f.tipo == TipoMovimiento.SALIDA_CONSUMO ||
            (f.tipo == TipoMovimiento.AJUSTE && !f.ajusteAumenta)
        val errores = ErroresMovimiento(
            producto = if (producto == null) "Elige un producto" else null,
            tipo = when {
                f.tipo == null -> "Elige el tipo de movimiento"
                f.tipo !in usuario.rol.tiposPermitidos -> "Tu perfil no puede registrar este tipo"
                else -> null
            },
            cantidad = when {
                cantidad == null || cantidad <= 0 -> "Ingresa un número entero mayor que 0"
                reduceStock && producto != null && cantidad > producto.stockActual ->   // RN-01
                    "No puede superar el stock disponible (${producto.stockActual})"
                else -> null
            },
            motivo = if (f.tipo == TipoMovimiento.AJUSTE && f.motivo.isBlank()) "El ajuste exige un motivo" else null // RN-04
        )
        _erroresMov.value = errores
        return !errores.hayErrores
    }

    fun guardarMovimiento(usuario: Usuario): Boolean {
        if (!validarMovimiento(usuario)) return false
        val f = _formMov.value
        val producto = productoPorCodigo(f.codigoProducto) ?: return false
        val tipo = f.tipo ?: return false
        val cantidad = f.cantidad.toInt()
        val movimiento = Movimiento(
            codigoProducto = producto.codigo,
            tipo = tipo,
            cantidad = if (tipo == TipoMovimiento.AJUSTE && !f.ajusteAumenta) -cantidad else cantidad,
            motivo = f.motivo.trim(),
            fecha = LocalDateTime.now(),            // RN-05: fecha, tipo, cantidad y quién lo registró
            usuario = usuario.nombre
        )
        val nuevoStock = producto.stockActual + movimiento.efecto
        viewModelScope.launch { repo.registrarMovimiento(movimiento) }   // movimiento + stock en una transacción
        if (nuevoStock <= producto.stockMinimo) _alertasStock.tryEmit(producto.copy(stockActual = nuevoStock))
        val aviso = if (nuevoStock <= producto.stockMinimo) " · Atención: producto en alerta de stock" else ""
        _mensajes.tryEmit("Movimiento registrado$aviso")
        _formMov.value = FormularioMovimientoEstado(tipo = usuario.rol.tiposPermitidos.singleOrNull())
        return true
    }
}
