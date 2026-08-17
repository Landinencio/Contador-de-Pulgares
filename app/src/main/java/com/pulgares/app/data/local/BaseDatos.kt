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

    @Query("SELECT * FROM gastos WHERE grupoId = :grupoId ORDER BY fechaMillis DESC")
    fun observaGastos(grupoId: String): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos ORDER BY fechaMillis DESC")
    fun observaTodos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE id = :id")
    suspend fun gasto(id: String): GastoEntity?

    @Upsert
    suspend fun guarda(gasto: GastoEntity)

    @Delete
    suspend fun borra(gasto: GastoEntity)

    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun borraPorId(id: String)
}

@Dao
interface PagosDao {

    @Query("SELECT * FROM pagos WHERE grupoId = :grupoId ORDER BY fechaMillis DESC")
    fun observaPagos(grupoId: String): Flow<List<PagoEntity>>

    @Query("SELECT * FROM pagos ORDER BY fechaMillis DESC")
    fun observaTodos(): Flow<List<PagoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guarda(pago: PagoEntity)

    @Query("DELETE FROM pagos WHERE id = :id")
    suspend fun borraPorId(id: String)
}

@Database(
    entities = [GrupoEntity::class, ColegaEntity::class, GastoEntity::class, PagoEntity::class],
    version = 2,
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

        fun obten(context: Context): BaseDatos = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(
                context.applicationContext,
                BaseDatos::class.java,
                "pulgares.db"
            )
                .addMigrations(DE_1_A_2)
                .build()
                .also { instancia = it }
        }
    }
}
