package cl.kaiju.stock.viewmodel

import androidx.lifecycle.ViewModel
import cl.kaiju.stock.model.Usuario
import cl.kaiju.stock.repository.DatosDePrueba
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Sabe quién está conectado y con qué perfil (RF-01).
class SesionViewModel : ViewModel() {

    val usuariosPrueba: List<Usuario> = DatosDePrueba.usuarios

    private val _usuario = MutableStateFlow<Usuario?>(null)       // null = nadie ha ingresado
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _seleccionado = MutableStateFlow<Usuario?>(null)  // lo elegido en el formulario de login
    val seleccionado: StateFlow<Usuario?> = _seleccionado.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    fun seleccionar(u: Usuario) {
        _seleccionado.value = u
        _errorLogin.value = null
    }

    // Validación del login: obligatorio elegir un usuario.
    fun ingresar(): Boolean {
        val elegido = _seleccionado.value
        if (elegido == null) {
            _errorLogin.value = "Elige un usuario para ingresar"
            return false
        }
        _usuario.value = elegido
        return true
    }

    fun cerrarSesion() {
        _usuario.value = null
        _seleccionado.value = null
    }
}
