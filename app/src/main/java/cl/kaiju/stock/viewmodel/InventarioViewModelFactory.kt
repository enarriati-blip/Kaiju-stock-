package cl.kaiju.stock.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cl.kaiju.stock.database.KaijuDatabase
import cl.kaiju.stock.repository.InventarioRepository

// Es la única pieza que necesita el Context (para crear la base). Arma el Repository y se lo entrega
// al ViewModel, que así sigue siendo Kotlin puro y se puede probar con un DAO falso.
class InventarioViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val repository = InventarioRepository(
        KaijuDatabase.obtenerInstancia(context).inventarioDao()
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Se verifica la clase pedida: si alguien pide otro ViewModel con esta fábrica, falla con un mensaje claro.
        require(modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            "ViewModel desconocido: ${modelClass.name}"
        }
        return InventarioViewModel(repository) as T
    }
}
