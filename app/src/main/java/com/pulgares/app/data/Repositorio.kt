package com.pulgares.app.data

import com.pulgares.app.data.local.BaseDatos
import com.pulgares.app.data.local.aDominio
import com.pulgares.app.data.local.aEntidad
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Grupo
import com.pulgares.app.domain.model.Pago
import com.pulgares.app.domain.settlement.Cuentas
import com.pulgares.app.domain.settlement.Saldo
import com.pulgares.app.domain.settlement.Transferencia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

/** Todo lo que hay que saber de un grupo para pintar su pantalla. */
data class EstadoGrupo(
    val grupo: Grupo,
    val gastos: List<Gasto>,
    val pagos: List<Pago>,
    val saldos: List<Saldo>,
    val plan: List<Transferencia>
) {
    val totalGastado: Long get() = Cuentas.totalGastado(gastos)
    val enPaz: Boolean get() = saldos.all { it.enPaz }
    val miSituacion: Cuentas.MiSituacion
        get() = Cuentas.loMioDelPlan(plan, grupo.yo?.id ?: "")

    fun saldoDe(colegaId: String): Long =
        saldos.firstOrNull { it.colegaId == colegaId }?.neto ?: 0L

    /** Dias desde el gasto sin saldar mas antiguo en el que participa el moroso. */
    fun diasDeudaDe(colegaId: String, ahora: Long): Int {
        val masViejo = gastos
            .filter { it.pagadorId != colegaId && it.deudas().containsKey(colegaId) }
            .minByOrNull { it.fechaMillis }
            ?: return 0
        val dias = (ahora - masViejo.fechaMillis) / 86_400_000L
        return dias.coerceAtLeast(0L).toInt()
    }
}

/**
 * La unica puerta a los datos. Combina las tres tablas y calcula los saldos y
 * el plan de pagos, para que la UI reciba todo mascado.
 */
class Repositorio(private val bd: BaseDatos) {

    fun observaGrupos(): Flow<List<Grupo>> =
        combine(bd.grupos().observaGrupos(), bd.grupos().observaTodosLosColegas()) { grupos, colegas ->
            grupos.map { grupo ->
                grupo.aDominio(colegas.filter { it.grupoId == grupo.id }.map { it.aDominio() })
            }
        }

    /** Resumen para la portada: saldo propio de cada grupo. */
    fun observaResumenGrupos(): Flow<List<ResumenGrupo>> = combine(
        observaGrupos(),
        bd.gastos().observaTodos(),
        bd.pagos().observaTodos()
    ) { grupos, gastos, pagos ->
        grupos.map { grupo ->
            val susGastos = gastos.filter { it.grupoId == grupo.id }.map { it.aDominio() }
            val susPagos = pagos.filter { it.grupoId == grupo.id }.map { it.aDominio() }
            val saldos = Cuentas.saldos(grupo.colegas, susGastos, susPagos)
            val plan = Cuentas.planDePagos(saldos)
            val mio = Cuentas.loMioDelPlan(plan, grupo.yo?.id ?: "")
            ResumenGrupo(
                grupo = grupo,
                cuantosGastos = susGastos.size,
                totalGastado = Cuentas.totalGastado(susGastos),
                miNeto = mio.neto,
                enPaz = saldos.all { it.enPaz }
            )
        }
    }

    fun observaGrupo(grupoId: String): Flow<EstadoGrupo?> = combine(
        bd.grupos().observaGrupo(grupoId),
        bd.grupos().observaColegas(grupoId),
        bd.gastos().observaGastos(grupoId),
        bd.pagos().observaPagos(grupoId)
    ) { grupo, colegas, gastos, pagos ->
        if (grupo == null) return@combine null
        val dominioGrupo = grupo.aDominio(colegas.map { it.aDominio() })
        val dominioGastos = gastos.map { it.aDominio() }
        val dominioPagos = pagos.map { it.aDominio() }
        val saldos = Cuentas.saldos(dominioGrupo.colegas, dominioGastos, dominioPagos)
        EstadoGrupo(
            grupo = dominioGrupo,
            gastos = dominioGastos,
            pagos = dominioPagos,
            saldos = saldos,
            plan = Cuentas.planDePagos(saldos)
        )
    }

    /** Mi avatar: se guarda en el colega marcado como "soyYo" de cualquier grupo. */
    fun observaMiAvatar(): Flow<String?> = bd.grupos().observaTodosLosColegas()
        .map { colegas -> colegas.firstOrNull { it.soyYo && it.avatar != null }?.avatar }

    suspend fun creaGrupo(nombre: String, emoji: String, nombresColegas: List<String>, miNombre: String): String {
        val grupoId = nuevoId()
        val ahora = System.currentTimeMillis()
        bd.grupos().guardaGrupo(
            Grupo(id = grupoId, nombre = nombre, emoji = emoji, creadoMillis = ahora).aEntidad()
        )
        val yo = Colega(id = nuevoId(), nombre = miNombre, soyYo = true)
        val otros = nombresColegas.filter { it.isNotBlank() }.map { Colega(id = nuevoId(), nombre = it.trim()) }
        bd.grupos().guardaColegas(
            (listOf(yo) + otros).mapIndexed { indice, colega -> colega.aEntidad(grupoId, indice) }
        )
        return grupoId
    }

    suspend fun renombraGrupo(grupo: Grupo, nombre: String, emoji: String) {
        bd.grupos().guardaGrupo(grupo.copy(nombre = nombre, emoji = emoji).aEntidad())
    }

    suspend fun guardaColegas(grupoId: String, colegas: List<Colega>) {
        bd.grupos().reemplazaColegas(
            grupoId,
            colegas.mapIndexed { indice, colega -> colega.aEntidad(grupoId, indice) }
        )
    }

    suspend fun anadeColega(grupoId: String, nombre: String, orden: Int) {
        bd.grupos().guardaColegas(
            listOf(Colega(id = nuevoId(), nombre = nombre.trim()).aEntidad(grupoId, orden))
        )
    }

    suspend fun borraGrupo(grupoId: String) = bd.grupos().borraGrupoEntero(grupoId)

    suspend fun guardaGasto(gasto: Gasto) = bd.gastos().guarda(gasto.aEntidad())

    suspend fun borraGasto(gastoId: String) = bd.gastos().borraPorId(gastoId)

    /** Pulgar arriba/abajo de un colega a un gasto. Volver a votar lo quita. */
    suspend fun votaGasto(gastoId: String, colegaId: String, arriba: Boolean) {
        val gasto = bd.gastos().gasto(gastoId)?.aDominio() ?: return
        val yaVotoIgual = if (arriba) colegaId in gasto.pulgaresArriba else colegaId in gasto.pulgaresAbajo
        val actualizado = gasto.copy(
            pulgaresArriba = when {
                arriba && yaVotoIgual -> gasto.pulgaresArriba - colegaId
                arriba -> gasto.pulgaresArriba + colegaId
                else -> gasto.pulgaresArriba - colegaId
            },
            pulgaresAbajo = when {
                !arriba && yaVotoIgual -> gasto.pulgaresAbajo - colegaId
                !arriba -> gasto.pulgaresAbajo + colegaId
                else -> gasto.pulgaresAbajo - colegaId
            }
        )
        bd.gastos().guarda(actualizado.aEntidad())
    }

    suspend fun registraPago(grupoId: String, deQuienId: String, aQuienId: String, importe: Long, nota: String?) {
        bd.pagos().guarda(
            Pago(
                id = nuevoId(),
                grupoId = grupoId,
                deQuienId = deQuienId,
                aQuienId = aQuienId,
                importeCentimos = importe,
                fechaMillis = System.currentTimeMillis(),
                nota = nota
            ).aEntidad()
        )
    }

    suspend fun borraPago(pagoId: String) = bd.pagos().borraPorId(pagoId)

    /** Cambia el avatar del colega que soy yo, en todos los grupos a la vez. */
    suspend fun guardaMiAvatar(avatar: String) {
        val mios = colegasActuales().filter { it.soyYo }
        bd.grupos().guardaColegas(mios.map { it.copy(avatar = avatar) })
    }

    suspend fun guardaAvatarDe(colegaId: String, avatar: String) {
        val colega = colegasActuales().firstOrNull { it.id == colegaId } ?: return
        bd.grupos().guardaColegas(listOf(colega.copy(avatar = avatar)))
    }

    private suspend fun colegasActuales() = bd.grupos().colegasDeUnaVez()

    companion object {
        fun nuevoId(): String = UUID.randomUUID().toString()
    }
}

/** Fila de la portada. */
data class ResumenGrupo(
    val grupo: Grupo,
    val cuantosGastos: Int,
    val totalGastado: Long,
    val miNeto: Long,
    val enPaz: Boolean
)
