package cl.kaiju.stock

import cl.kaiju.stock.model.Producto
import cl.kaiju.stock.model.Rol
import cl.kaiju.stock.model.TipoMovimiento
import cl.kaiju.stock.model.Usuario
import cl.kaiju.stock.repository.InventarioRepository
import cl.kaiju.stock.viewmodel.InventarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// Tests unitarios: corren en el computador, sin emulador. Ejecutar: ./gradlew testDebugUnitTest
// Dispatchers.setMain: en un test no existe el hilo principal de Android; viewModelScope necesita uno.
@OptIn(ExperimentalCoroutinesApi::class)
class InventarioViewModelTest {

    private val encargado = Usuario(3, "Encargado Demo", Rol.ENCARGADO_INVENTARIO)
    private val vendedor = Usuario(2, "Vendedor Demo", Rol.VENDEDOR)

    @Before fun antes() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun despues() { Dispatchers.resetMain() }

    // Cada VM nace con un DAO falso vacío; el ViewModel carga los datos de prueba al iniciar.
    private fun crearVm(dao: FakeInventarioDao = FakeInventarioDao()) =
        InventarioViewModel(InventarioRepository(dao))

    @Test
    fun rn01_salidaMayorAlStock_esRechazada() {
        val vm = crearVm()
        vm.iniciarFormMovimiento("KJ-001", encargado)          // KJ-001 tiene 25 unidades
        vm.onMovTipo(TipoMovimiento.SALIDA_VENTA)
        vm.onMovCantidad("999")
        assertFalse(vm.guardarMovimiento(encargado))
        assertNotNull(vm.erroresMov.value.cantidad)
    }

    @Test
    fun rn02_precioCero_esRechazado() {
        val vm = crearVm()
        vm.iniciarFormProducto(null)
        vm.onCodigo("KJ-900"); vm.onNombre("Polera Test"); vm.onCategoria("Poleras")
        vm.onPrecio("0"); vm.onStockMinimo("2")
        assertFalse(vm.validarProducto())
        assertNotNull(vm.erroresProducto.value.precio)
    }

    @Test
    fun rn03_codigoRepetido_esRechazado() {
        val vm = crearVm()
        vm.iniciarFormProducto(null)
        vm.onCodigo("KJ-001"); vm.onNombre("Duplicado"); vm.onCategoria("Poleras")
        vm.onPrecio("1000"); vm.onStockMinimo("1")
        assertFalse(vm.validarProducto())
        assertNotNull(vm.erroresProducto.value.codigo)
    }

    @Test
    fun rn04_ajusteSinMotivo_esRechazado() {
        val vm = crearVm()
        vm.iniciarFormMovimiento("KJ-001", encargado)
        vm.onMovTipo(TipoMovimiento.AJUSTE)
        vm.onMovCantidad("2")
        assertFalse(vm.validarMovimiento(encargado))
        assertNotNull(vm.erroresMov.value.motivo)
    }

    @Test
    fun entrada_aumentaElStock() {
        val vm = crearVm()
        vm.iniciarFormMovimiento("KJ-001", encargado)
        vm.onMovTipo(TipoMovimiento.ENTRADA)
        vm.onMovCantidad("5")
        assertTrue(vm.guardarMovimiento(encargado))
        assertEquals(30, vm.productos.value.first { it.codigo == "KJ-001" }.stockActual)
    }

    @Test
    fun rn06_productoEnAlerta_cuandoStockIgualOMenorAlMinimo() {
        val vm = crearVm()
        // KJ-006 tiene 6 unidades y mínimo 6: está justo en el límite
        assertTrue(vm.productos.value.first { it.codigo == "KJ-006" }.enAlerta)
    }

    @Test
    fun vendedor_noPuedeRegistrarEntradas() {
        val vm = crearVm()
        vm.iniciarFormMovimiento("KJ-001", vendedor)
        vm.onMovTipo(TipoMovimiento.ENTRADA)
        vm.onMovCantidad("5")
        assertFalse(vm.validarMovimiento(vendedor))
        assertNotNull(vm.erroresMov.value.tipo)
    }

    // ---------- Recursos nativos (lo que depende del ViewModel) ----------

    @Test
    fun guardarFoto_guardaLaRutaEnElProducto() {
        val vm = crearVm()
        vm.guardarFoto("KJ-001", "/ruta/foto.jpg")
        assertEquals("/ruta/foto.jpg", vm.productos.value.first { it.codigo == "KJ-001" }.imagenRuta)
    }

    @Test
    fun editarProducto_noPierdeLaFoto() {
        val vm = crearVm()
        vm.guardarFoto("KJ-001", "/ruta/foto.jpg")
        vm.iniciarFormProducto("KJ-001")
        vm.onPrecio("15990")
        assertTrue(vm.guardarProducto())
        assertEquals("/ruta/foto.jpg", vm.productos.value.first { it.codigo == "KJ-001" }.imagenRuta)
    }

    // ---------- Persistencia ----------

    @Test
    fun persistencia_arrancaConLosDatosQueYaExistianEnElDao() {
        // Simula "cerrar y reabrir la app": el DAO ya trae un producto guardado.
        val dao = FakeInventarioDao()
        kotlinx.coroutines.runBlocking {
            dao.guardarProducto(Producto("KJ-777", "Guardado antes", "", "Poleras", "Ropa", 9990, 5, 1))
        }
        val vm = crearVm(dao)
        assertEquals(1, vm.productos.value.size)              // no se cargaron los datos de prueba encima
        assertEquals("KJ-777", vm.productos.value.first().codigo)
    }

    @Test
    fun persistencia_productoCreadoSobreviveAUnViewModelNuevo() {
        val dao = FakeInventarioDao()
        val vm1 = crearVm(dao)
        vm1.iniciarFormProducto(null)
        vm1.onCodigo("KJ-500"); vm1.onNombre("Polera Nueva"); vm1.onCategoria("Poleras")
        vm1.onPrecio("14990"); vm1.onStockMinimo("3")
        assertTrue(vm1.guardarProducto())

        val vm2 = crearVm(dao)                                  // "reabrir la app": ViewModel nuevo, mismo almacenamiento
        assertTrue(vm2.productos.value.any { it.codigo == "KJ-500" })
        assertEquals(0, vm2.productos.value.first { it.codigo == "KJ-500" }.stockActual)  // producto nuevo parte en 0
    }
}
