package cl.kaiju.stock.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.kaiju.stock.model.Movimiento
import cl.kaiju.stock.model.Producto

// Versión 2: se agregó la columna "imagenRuta" a productos (foto tomada con la cámara).
// exportSchema = false: el curso no exporta el esquema de la base.
@Database(entities = [Producto::class, Movimiento::class], version = 2, exportSchema = false)
@TypeConverters(Convertidores::class)
abstract class KaijuDatabase : RoomDatabase() {

    abstract fun inventarioDao(): InventarioDao

    companion object {
        // MIGRACIÓN 1 -> 2: le dice a Room cómo pasar de la versión 1 a la 2 SIN borrar los datos guardados.
        // Cambiar solo "version" no alcanza: Room exige este objeto para no perder información al actualizar.
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE productos ADD COLUMN imagenRuta TEXT NOT NULL DEFAULT ''")
            }
        }

        // @Volatile + synchronized: evita crear dos bases de datos a la vez (patrón singleton).
        @Volatile private var instancia: KaijuDatabase? = null

        fun obtenerInstancia(contexto: Context): KaijuDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,   // applicationContext: no retiene ninguna Activity
                    KaijuDatabase::class.java,
                    "kaiju.db"
                ).addMigrations(MIGRACION_1_2).build().also { instancia = it }
            }
    }
}
