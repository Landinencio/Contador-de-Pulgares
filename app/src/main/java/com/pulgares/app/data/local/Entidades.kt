package com.pulgares.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pulgares.app.domain.model.Categoria
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Grupo
import com.pulgares.app.domain.model.Pago
import com.pulgares.app.domain.model.Reparto

/**
 * Tablas de la base local. Todo vive en el movil: la app funciona entera sin
 * internet y sin cuentas, que es medio motivo de existir de este proyecto.
 *
 * El reparto de un gasto y los pulgares se guardan como texto plano en vez de
 * en tablas aparte: son listas cortas que solo se leen junto al gasto, y asi
 * no hace falta un join por cada linea de la lista.
 */

@Entity(tableName = "grupos")
data class GrupoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val emoji: String,
    val creadoMillis: Long,
    val codigo: String?
)

@Entity(
    tableName = "colegas",
    indices = [Index("grupoId")]
)
data class ColegaEntity(
    @PrimaryKey val id: String,
    val grupoId: String,
    val nombre: String,
    val avatar: String?,
    val soyYo: Boolean,
    val orden: Int
)

@Entity(
    tableName = "gastos",
    indices = [Index("grupoId")]
)
data class GastoEntity(
    @PrimaryKey val id: String,
    val grupoId: String,
    val concepto: String,
    val importeCentimos: Long,
    val pagadorId: String,
    val fechaMillis: Long,
    val categoria: String,
    /** "escote:a,b" | "partes:a=2,b=1" | "exacto:a=1000,b=550" */
    val reparto: String,
    val nota: String?,
    val pulgaresArriba: String,
    val pulgaresAbajo: String
)

@Entity(
    tableName = "pagos",
    indices = [Index("grupoId")]
)
data class PagoEntity(
    @PrimaryKey val id: String,
    val grupoId: String,
    val deQuienId: String,
    val aQuienId: String,
    val importeCentimos: Long,
    val fechaMillis: Long,
    val nota: String?
)

// ---- conversiones a dominio ----

fun ColegaEntity.aDominio() = Colega(id = id, nombre = nombre, avatar = avatar, soyYo = soyYo)

fun Colega.aEntidad(grupoId: String, orden: Int) = ColegaEntity(
    id = id,
    grupoId = grupoId,
    nombre = nombre,
    avatar = avatar,
    soyYo = soyYo,
    orden = orden
)

fun GrupoEntity.aDominio(colegas: List<Colega>) = Grupo(
    id = id,
    nombre = nombre,
    emoji = emoji,
    colegas = colegas,
    creadoMillis = creadoMillis,
    codigo = codigo
)

fun Grupo.aEntidad() = GrupoEntity(
    id = id,
    nombre = nombre,
    emoji = emoji,
    creadoMillis = creadoMillis,
    codigo = codigo
)

fun GastoEntity.aDominio() = Gasto(
    id = id,
    grupoId = grupoId,
    concepto = concepto,
    importeCentimos = importeCentimos,
    pagadorId = pagadorId,
    fechaMillis = fechaMillis,
    categoria = Categoria.porNombre(categoria),
    reparto = RepartoTexto.parse(reparto),
    nota = nota,
    pulgaresArriba = ids(pulgaresArriba),
    pulgaresAbajo = ids(pulgaresAbajo)
)

fun Gasto.aEntidad() = GastoEntity(
    id = id,
    grupoId = grupoId,
    concepto = concepto,
    importeCentimos = importeCentimos,
    pagadorId = pagadorId,
    fechaMillis = fechaMillis,
    categoria = categoria.name,
    reparto = RepartoTexto.serializa(reparto),
    nota = nota,
    pulgaresArriba = pulgaresArriba.joinToString(","),
    pulgaresAbajo = pulgaresAbajo.joinToString(",")
)

fun PagoEntity.aDominio() = Pago(
    id = id,
    grupoId = grupoId,
    deQuienId = deQuienId,
    aQuienId = aQuienId,
    importeCentimos = importeCentimos,
    fechaMillis = fechaMillis,
    nota = nota
)

fun Pago.aEntidad() = PagoEntity(
    id = id,
    grupoId = grupoId,
    deQuienId = deQuienId,
    aQuienId = aQuienId,
    importeCentimos = importeCentimos,
    fechaMillis = fechaMillis,
    nota = nota
)

private fun ids(bruto: String): Set<String> =
    bruto.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

/** El reparto de un gasto, aplanado a texto para guardarlo en una columna. */
object RepartoTexto {

    fun serializa(reparto: Reparto): String = when (reparto) {
        is Reparto.Escote -> "escote:" + reparto.entre.joinToString(",")
        is Reparto.PorPartes -> "partes:" + reparto.pesos.entries.joinToString(",") { "${it.key}=${it.value}" }
        is Reparto.Exacto -> "exacto:" + reparto.importes.entries.joinToString(",") { "${it.key}=${it.value}" }
    }

    fun parse(bruto: String): Reparto {
        val corte = bruto.indexOf(':')
        if (corte < 0) return Reparto.Escote(emptyList())
        val tipo = bruto.substring(0, corte)
        val cuerpo = bruto.substring(corte + 1)
        val trozos = cuerpo.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        return when (tipo) {
            "partes" -> Reparto.PorPartes(
                trozos.mapNotNull { trozo ->
                    val partes = trozo.split("=")
                    val peso = partes.getOrNull(1)?.toIntOrNull()
                    if (partes.size == 2 && peso != null) partes[0] to peso else null
                }.toMap()
            )

            "exacto" -> Reparto.Exacto(
                trozos.mapNotNull { trozo ->
                    val partes = trozo.split("=")
                    val importe = partes.getOrNull(1)?.toLongOrNull()
                    if (partes.size == 2 && importe != null) partes[0] to importe else null
                }.toMap()
            )

            else -> Reparto.Escote(trozos)
        }
    }
}
