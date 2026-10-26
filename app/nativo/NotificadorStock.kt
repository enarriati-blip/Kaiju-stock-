package cl.kaiju.stock.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.kaiju.stock.MainActivity
import cl.kaiju.stock.model.Producto

// RECURSO NATIVO 1: NOTIFICACIONES.
// Necesidad del caso: avisar al encargado, aunque la app esté en segundo plano, cuando un producto llega
// a su stock mínimo (RN-06), para reponer a tiempo y evitar quiebres de stock.
// Permiso: POST_NOTIFICATIONS (peligroso desde Android 13): se declara en el Manifest y se pide en ejecución.
class NotificadorStock(private val contexto: Context) {

    companion object {
        private const val CANAL_ID = "stock_bajo"
    }

    init { crearCanal() }

    // Desde Android 8 toda notificación debe pertenecer a un "canal" que el usuario puede configurar.
    private fun crearCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL_ID, "Alertas de stock", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisa cuando un producto llega al stock mínimo"
            }
            contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    // Antes de Android 13 el permiso no existe (se considera concedido).
    fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")   // el permiso se verifica justo arriba con tienePermiso()
    fun avisarStockBajo(producto: Producto) {
        if (!tienePermiso()) return      // sin permiso la app sigue funcionando, solo que sin avisos

        // Al tocar la notificación se abre la app.
        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        // FLAG_IMMUTABLE: nadie más puede modificar el Intent (medida de seguridad exigida desde Android 12).
        val pendiente = PendingIntent.getActivity(
            contexto, producto.codigo.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(contexto, CANAL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Stock bajo: ${producto.nombre}")
            .setContentText("Quedan ${producto.stockActual} unidades (mínimo ${producto.stockMinimo})")
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()
        // El id es el código del producto: si vuelve a avisar, reemplaza la notificación anterior en vez de duplicarla.
        NotificationManagerCompat.from(contexto).notify(producto.codigo.hashCode(), notificacion)
    }
}
