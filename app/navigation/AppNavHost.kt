package cl.kaiju.stock.navigation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.kaiju.stock.nativo.NotificadorStock
import cl.kaiju.stock.ui.screen.*
import cl.kaiju.stock.ui.theme.KaijuTheme
import cl.kaiju.stock.viewmodel.InventarioViewModel
import cl.kaiju.stock.viewmodel.InventarioViewModelFactory
import cl.kaiju.stock.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavHost() {
    KaijuTheme {
        val nav = rememberNavController()
        val contexto = LocalContext.current
        val scope = rememberCoroutineScope()

        // Dos ViewModels creados UNA vez aquí y pasados a las pantallas = estado compartido.
        // La fábrica le entrega al ViewModel su Repository (que usa Room). remember: se crea una sola vez.
        val sesionVm: SesionViewModel = viewModel()
        val fabrica = remember { InventarioViewModelFactory(contexto) }
        val inventarioVm: InventarioViewModel = viewModel(factory = fabrica)

        val usuario by sesionVm.usuario.collectAsState()
        val u = usuario
        val alertas by inventarioVm.alertas.collectAsState(initial = emptyList())
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(Unit) { inventarioVm.mensajes.collect { snackbar.showSnackbar(it) } }

        // ---------- RECURSO NATIVO: NOTIFICACIONES ----------
        val notificador = remember { NotificadorStock(contexto) }

        // Permiso POST_NOTIFICATIONS (Android 13+) en tiempo de ejecución. Si se niega, la app sigue
        // funcionando sin avisos y se le explica al usuario qué pasó.
        val lanzadorPermisoNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (!concedido) scope.launch { snackbar.showSnackbar("Sin permiso de notificaciones no recibirás avisos de stock bajo") }
        }
        // Se pide una vez que el usuario ingresó (en contexto), no al abrir la app.
        LaunchedEffect(u) {
            if (u != null && !notificador.tienePermiso()) lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // El ViewModel avisa QUE un producto quedó en alerta; la capa de Android muestra la notificación.
        LaunchedEffect(Unit) { inventarioVm.alertasStock.collect { notificador.avisarStockBajo(it) } }

        val entrada by nav.currentBackStackEntryAsState()
        val rutaActual = entrada?.destination?.route

        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                // Sin sesión (o en el login) no hay menú. El menú depende del perfil: el vendedor no ve Alertas.
                if (u != null && rutaActual != Rutas.Login.ruta) {
                    NavigationBar {
                        Rutas.menu.filter { it != Rutas.Alertas || u.rol.veAlertas }.forEach { r ->
                            NavigationBarItem(
                                selected = rutaActual == r.ruta,
                                onClick = {
                                    if (r == Rutas.Movimiento) inventarioVm.iniciarFormMovimiento(null, u)
                                    nav.navigate(r.destino) {
                                        popUpTo(Rutas.Catalogo.ruta)   // evita apilar pantallas al tocar el menú
                                        launchSingleTop = true
                                    }
                                },
                                // Globo con la cantidad de productos en alerta (retroalimentación visual).
                                icon = {
                                    BadgedBox(badge = {
                                        if (r == Rutas.Alertas && alertas.isNotEmpty()) Badge { Text("${alertas.size}") }
                                    }) { Icon(r.icono, contentDescription = r.titulo) }
                                },
                                label = { Text(r.titulo) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                nav, startDestination = Rutas.Login.ruta, modifier = Modifier.padding(padding),
                // ANIMACIÓN (propósito: fluidez): al cambiar de pantalla hay un fundido y un leve desplazamiento
                // en vez de un corte brusco, y se entiende que se "avanza" a otra vista.
                enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 10 } },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(300)) },
                popExitTransition = { fadeOut(tween(200)) }
            ) {

                composable(Rutas.Login.ruta) {
                    PantallaLogin(sesionVm, onIngreso = {
                        nav.navigate(Rutas.Catalogo.ruta) { popUpTo(Rutas.Login.ruta) { inclusive = true } }
                    })
                }

                composable(Rutas.Catalogo.ruta) {
                    u?.let {
                        PantallaCatalogo(
                            vm = inventarioVm, usuario = it,
                            onDetalle = { codigo -> nav.navigate(Rutas.Detalle.conCodigo(codigo)) },
                            onNuevo = {
                                inventarioVm.iniciarFormProducto(null)
                                nav.navigate(Rutas.FormularioProducto.conCodigo(null))
                            },
                            onSalir = {
                                sesionVm.cerrarSesion()
                                nav.navigate(Rutas.Login.ruta) { popUpTo(Rutas.Catalogo.ruta) { inclusive = true } }
                            }
                        )
                    }
                }

                composable(
                    route = Rutas.Detalle.ruta,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType })
                ) { backStackEntry ->
                    // El argumento viaja dentro de la NavBackStackEntry.
                    val codigo = backStackEntry.arguments?.getString("codigo") ?: ""
                    u?.let {
                        PantallaDetalleProducto(
                            vm = inventarioVm, usuario = it, codigo = codigo,
                            onEditar = { c ->
                                inventarioVm.iniciarFormProducto(c)
                                nav.navigate(Rutas.FormularioProducto.conCodigo(c))
                            },
                            onMovimiento = { c ->
                                inventarioVm.iniciarFormMovimiento(c, it)
                                nav.navigate(Rutas.Movimiento.conCodigo(c))
                            }
                        )
                    }
                }

                composable(
                    route = Rutas.FormularioProducto.ruta,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) {
                    PantallaFormularioProducto(inventarioVm, onGuardado = { nav.popBackStack() })
                }

                composable(
                    route = Rutas.Movimiento.ruta,
                    arguments = listOf(navArgument("codigo") { type = NavType.StringType; nullable = true; defaultValue = null })
                ) {
                    u?.let { PantallaMovimiento(inventarioVm, it, onGuardado = { nav.popBackStack() }) }
                }

                composable(Rutas.Alertas.ruta) {
                    PantallaAlertas(inventarioVm, onDetalle = { codigo -> nav.navigate(Rutas.Detalle.conCodigo(codigo)) })
                }
            }
        }
    }
}
