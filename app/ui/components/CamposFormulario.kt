package cl.kaiju.stock.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.layout.fillMaxWidth

// Campo de texto con retroalimentación visual: isError (rojo), supportingText (mensaje),
// leadingIcon (qué es el campo) y trailingIcon de advertencia cuando hay error (IE 2.1.2).
@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    error: String?,
    icono: ImageVector,
    numerico: Boolean = false,
    enabled: Boolean = true,
    unaLinea: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = { if (error != null) Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) },
        isError = error != null,
        supportingText = { error?.let { Text(it) } },
        singleLine = unaLinea,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = if (numerico) KeyboardType.Number else KeyboardType.Text),
        modifier = Modifier.fillMaxWidth()
    )
}

// Selector de lista fija (DropdownMenu). El usuario elige; no escribe.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSelector(
    valorMostrado: String,
    etiqueta: String,
    opciones: List<String>,
    onElegir: (Int) -> Unit,          // recibe la posición elegida
    error: String?,
    icono: ImageVector,
    enabled: Boolean = true
) {
    var expandido by rememberSaveable { mutableStateOf(false) }   // sobrevive a la rotación
    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { if (enabled) expandido = !expandido }) {
        OutlinedTextField(
            value = valorMostrado,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEachIndexed { i, texto ->
                DropdownMenuItem(text = { Text(texto) }, onClick = { onElegir(i); expandido = false })
            }
        }
    }
}
