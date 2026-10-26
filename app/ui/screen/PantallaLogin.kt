package cl.kaiju.stock.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.kaiju.stock.ui.components.CampoSelector
import cl.kaiju.stock.viewmodel.SesionViewModel

@Composable
fun PantallaLogin(vm: SesionViewModel, onIngreso: () -> Unit) {
    val seleccionado by vm.seleccionado.collectAsState()
    val error by vm.errorLogin.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Kaiju Stock", style = MaterialTheme.typography.headlineLarge)
        Text("Control de inventario", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        CampoSelector(
            valorMostrado = seleccionado?.let { "${it.nombre} · ${it.rol.etiqueta}" } ?: "",
            etiqueta = "Usuario de prueba",
            opciones = vm.usuariosPrueba.map { "${it.nombre} · ${it.rol.etiqueta}" },
            onElegir = { i -> vm.seleccionar(vm.usuariosPrueba[i]) },
            error = error,
            icono = Icons.Default.Person
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = { if (vm.ingresar()) onIngreso() }, modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
        Spacer(Modifier.height(16.dp))
        Text("Usuarios y datos ficticios (proyecto académico)", style = MaterialTheme.typography.labelSmall)
    }
}
