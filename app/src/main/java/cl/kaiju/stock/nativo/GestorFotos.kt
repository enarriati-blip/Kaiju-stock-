package cl.kaiju.stock.nativo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

// RECURSO NATIVO 2: CÁMARA.
// Necesidad del caso: identificar visualmente cada producto del catálogo con una foto tomada en la tienda.
// Permiso: CAMERA (peligroso): se declara en el Manifest y se pide en ejecución antes de abrir la cámara.
// Seguridad: la foto se guarda en el almacenamiento PRIVADO de la app (no necesita permiso de almacenamiento)
// y la cámara escribe a través de un FileProvider, que entrega un permiso temporal solo para ese archivo.
object GestorFotos {

    // Carpeta privada de la app: otras apps no pueden leerla y se borra al desinstalar.
    fun crearArchivo(contexto: Context, codigo: String): File {
        val carpeta = contexto.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: contexto.filesDir
        return File(carpeta, "producto_${codigo}_${System.currentTimeMillis()}.jpg")
    }

    // content:// URI temporal que se le entrega a la app de cámara para que guarde la foto.
    fun uriDe(contexto: Context, archivo: File): Uri =
        FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)

    // Lee la foto reduciendo su tamaño (una foto completa pesa mucho y puede agotar la memoria)
    // y corrige la rotación guardada por la cámara (EXIF).
    fun cargarBitmap(ruta: String, maxLado: Int = 1024): Bitmap? {
        if (ruta.isBlank() || !File(ruta).exists()) return null
        val bordes = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, bordes)
        var muestra = 1
        while (bordes.outWidth / muestra > maxLado || bordes.outHeight / muestra > maxLado) muestra *= 2
        val bitmap = BitmapFactory.decodeFile(ruta, BitmapFactory.Options().apply { inSampleSize = muestra }) ?: return null

        val grados = when (ExifInterface(ruta).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        return if (grados == 0f) bitmap
        else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(grados) }, true)
    }
}
