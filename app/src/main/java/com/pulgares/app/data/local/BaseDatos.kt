package com.pulgares.app.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface GruposDao {

    @Query("SELECT * FROM grupos ORDER BY creadoMillis DESC")
    fun observaGrupos(): Flow<List<GrupoEntity>>

    @Query("SELECT * FROM grupos WHERE id = :id")
    fun observaGrupo(id: String): Flow<GrupoEntity?>

    @Query("SELECT * FROM colegas ORDER BY orden ASC")
    fun observaTodosLosColegas(): Flow<List<ColegaEntity>>

    @Query("SELECT * FROM colegas WHERE grupoId = :grupoId ORDER BY orden ASC")
    fun observaColegas(grupoId: String): Flow<List<ColegaEntity>>

    /** Lectura puntual (no reactiva) para editar avatares y nombres. */
    @Query("SELECT * FROM colegas")
    suspend fun colegasDeUnaVez(): List<ColegaEntity>

    @Query("SELECT * FROM grupos WHERE id = :grupoId")
    suspend fun grupoDeUnaVez(grupoId: String): GrupoEntity?

    @Query("SELECT * FROM colegas WHERE grupoId = :grupoId ORDER BY orden ASC")
    suspend fun colegasDeUnGrupo(grupoId: String): List<ColegaEntity>

    /** Para no bajar dos veces el mismo grupo compartido. */
    @Query("SELECT * FROM grupos WHERE remotoId = :remotoId LIMIT 1")
    suspend fun grupoPorRemoto(remotoId: String): GrupoEntity?

    @Upsert
    suspend fun guardaGrupo(grupo: GrupoEntity)

    @Upsert
    suspend fun guardaColegas(colegas: List<ColegaEntity>)

    @Query("DELETE FROM colegas WHERE grupoId = :grupoId")
    suspend fun borraColegasDe(grupoId: String)

    @Transaction
    suspend fun reemplazaColegas(grupoId: String, colegas: List<ColegaEntity>) {
        borraColegasDe(grupoId)
        guardaColegas(colegas)
    }

    @Query("DELETE FROM grupos WHERE id = :grupoId")
    suspend fun borraGrupo(grupoId: String)

    @Query("DELETE FROM gastos WHERE grupoId = :grupoId")
    suspend fun borraGastosDe(grupoId: String)

    @Query("DELETE FROM pagos WHERE grupoId = :grupoId")
    suspend fun borraPagosDe(grupoId: String)

    /** Borrar un grupo se lleva por delante a sus colegas, gastos y pagos. */
    @Transaction
    suspend fun borraGrupoEntero(grupoId: String) {
        borraGastosDe(grupoId)
        borraPagosDe(grupoId)
        borraColegasDe(grupoId)
        borraGrupo(grupoId)
    }
}

@Dao
interface GastosDao {

    @Query("SELECT * FROM gastos WHERE grupoId = :grupoId AND borrado = 0 ORDER BY fechaMillis DESC")
    fun observaGastos(grupoId: String): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE borrado = 0 ORDER BY fechaMillis DESC")
    fun observaTodos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun gasto(id: String): GastoEntity?

    @Query("SELECT * FROM gastos WHERE grupoId = :grupoId ORDER BY fechaMillis DESC")
    suspend fun gastosDeUnaVez(grupoId: String): List<GastoEntity>

    @Upsert
    suspend fun guarda(gasto: GastoEntity)

    @Delete
    suspend fun borra(gasto: GastoEntity)

    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun borraPorId(id: String)
}

@Dao
interface PagosDao {

    @Query("SELECT * FROM pagos WHERE grupoId = :grupoId AND borrado = 0 ORDER BY fechaMillis DESC")
    fun observaPagos(grupoId: String): Flow<List<PagoEntity>>

    @Query("SELECT * FROM pagos WHERE borrado = 0 ORDER BY fechaMillis DESC")
    fun observaTodos(): Flow<List<PagoEntity>>

    @Query("SELECT * FROM pagos WHERE grupoId = :grupoId ORDER BY fechaMillis DESC")
    suspend fun pagosDeUnaVez(grupoId: String): List<PagoEntity>

    @Query("SELECT * FROM pagos WHERE id = :id")
    suspend fun pagoPorId(id: String): PagoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guarda(pago: PagoEntity)

    @Query("DELETE FROM pagos WHERE id = :id")
    suspend fun borraPorId(id: String)
}

@Database(
    entities = [GrupoEntity::class, ColegaEntity::class, GastoEntity::class, PagoEntity::class],
    version = 5,
    exportSchema = true
)
abstract class BaseDatos : RoomDatabase() {
    abstract fun grupos(): GruposDao
    abstract fun gastos(): GastosDao
    abstract fun pagos(): PagosDao

    companion object {
        @Volatile
        private var instancia: BaseDatos? = null

        /**
         * v1 -> v2: los colegas ganan `activo`. Quien sale del grupo se marca en
         * vez de borrarse, para que sus gastos sigan teniendo nombre. Todos los
         * que ya existen entran como activos.
         */
        private val DE_1_A_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE colegas ADD COLUMN activo INTEGER NOT NULL DEFAULT 1")
            }
        }

        /**
         * v2 -> v3: gastos y pagos ganan `version` para poder sincronizar entre
         * moviles sin que una copia vieja pise una edicion nueva. Lo que ya
         * existe entra con 0, que es "lo mas viejo posible": la primera
         * sincronizacion lo subira con su version de verdad.
         */
        private val DE_2_A_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gastos ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pagos ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * v3 -> v4: los grupos guardan su id en la nube y su version. El id
         * remoto no puede ser el local porque el backend genera el suyo propio.
         */
        private val DE_3_A_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE grupos ADD COLUMN remotoId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE grupos ADD COLUMN version INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * v4 -> v5: lapidas. En un grupo compartido, borrar de verdad haria que
         * el otro movil devolviera la fila en la siguiente sincronizacion; se
         * marca `borrado` y la marca viaja como cualquier edicion.
         */
        private val DE_4_A_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE gastos ADD COLUMN borrado INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pagos ADD COLUMN borrado INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun obten(context: Context): BaseDatos = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                BaseDatos::class.java,
                "pulgares.db"
            )
                .addMigrations(DE_1_A_2, DE_2_A_3, DE_3_A_4, DE_4_A_5)
                .build()
                .also { instancia = it }
        }
    }
}
