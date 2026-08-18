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

    /**
     * El estado del grupo leido de una vez, sin flujos. Lo usa la sincronizacion,
     * que necesita una foto fija para subir y no un flujo que cambia debajo.
     */
    suspend fun grupoDeUnaVez(grupoId: String): EstadoGrupo? {
        val grupo = bd.grupos().grupoDeUnaVez(grupoId) ?: return null
        val colegas = bd.grupos().colegasDeUnGrupo(grupoId).map { it.aDominio() }
        val gastos = bd.gastos().gastosDeUnaVez(grupoId).map { it.aDominio() }
        val pagos = bd.pagos().pagosDeUnaVez(grupoId).map { it.aDominio() }
        val saldos = Cuentas.saldos(colegas, gastos, pagos)
        return EstadoGrupo(
            grupo = grupo.aDominio(colegas),
            gastos = gastos,
            pagos = pagos,
            saldos = saldos,
            plan = Cuentas.planDePagos(saldos)
        )
    }

    /**
     * Mi avatar. Todos los "yo" de todos los grupos comparten monigote, asi que
     * vale con el primero que tenga uno; se ordena por grupo para que la eleccion
     * sea siempre la misma y no dependa de como le apetezca devolver las filas a
     * SQLite.
     */
    fun observaMiAvatar(): Flow<String?> = bd.grupos().observaTodosLosColegas()
        .map { colegas ->
            colegas.filter { it.soyYo && it.avatar != null }
                .minByOrNull { it.grupoId }
                ?.avatar
        }

    /**
     * Mi colega "yo" de cualquier grupo, para sugerir el perfil en el primer
     * arranque a quien ya usaba la app: que no tenga que escribir su nombre otra
     * vez si la app ya lo sabia.
     */
    suspend fun miYoMasReciente(): Colega? =
        bd.grupos().colegasDeUnaVez()
            .filter { it.soyYo }
            .maxByOrNull { it.grupoId }
            ?.aDominio()

    /** Cambia mi nombre en todos los grupos a la vez (al editar el perfil). */
    suspend fun renombraMisYo(nombre: String) {
        val mios = bd.grupos().colegasDeUnaVez().filter { it.soyYo }
        if (mios.isNotEmpty()) {
            bd.grupos().guardaColegas(mios.map { it.copy(nombre = nombre.trim()) })
        }
    }

    /**
     * Crea un grupo. El unico colega inicial soy yo, con el nombre y el monigote
     * del perfil: el resto de la gente entra pidiendolo con el codigo (y cada uno
     * trae su nombre), o se anade a mano desde los ajustes para grupos sin nube.
     */
    suspend fun creaGrupo(nombre: String, emoji: String, miNombre: String, miAvatar: String?): String {
        val grupoId = nuevoId()
        val ahora = System.currentTimeMillis()
        bd.grupos().guardaGrupo(
            Grupo(id = grupoId, nombre = nombre, emoji = emoji, creadoMillis = ahora).aEntidad()
        )
        val yo = Colega(id = nuevoId(), nombre = miNombre, avatar = miAvatar, soyYo = true)
        bd.grupos().guardaColegas(listOf(yo.aEntidad(grupoId, 0)))
        return grupoId
    }

    suspend fun renombraGrupo(grupo: Grupo, nombre: String, emoji: String) {
        bd.grupos().guardaGrupo(
            grupo.copy(
                nombre = nombre,
                emoji = emoji,
                // El nombre se edita: sube la version para ganar al sincronizar.
                version = System.currentTimeMillis()
            ).aEntidad()
        )
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

    /** Los ids locales de los grupos que estan compartidos en la nube. */
    suspend fun idsDeGruposCompartidos(): List<String> =
        bd.grupos().gruposDeUnaVez().filter { it.remotoId != null }.map { it.id }

    /**
     * Guarda un gasto. Se comprueba que el reparto sume el importe: la pantalla ya
     * lo valida, asi que un descuadre aqui solo puede venir de un error de codigo
     * o de un dato importado, y colarlo dejaria los saldos del grupo sin sumar
     * cero para siempre, con un residuo que ninguna pantalla sabe explicar.
     */
    suspend fun guardaGasto(gasto: Gasto) {
        require(gasto.cuadra) {
            "El reparto de «${gasto.concepto}» suma ${gasto.deudas().values.sum()} " +
                "en vez de ${gasto.importeCentimos}"
        }
        bd.gastos().guarda(gasto.aEntidad())
    }

    /**
     * Borra un gasto. En un grupo compartido no se borra la fila: se deja una
     * lapida (borrado=true, version nueva) que viaja al sincronizar; borrar de
     * verdad haria que el otro movil devolviera el gasto en la siguiente
     * sincronizacion. En un grupo solo-local, fuera la fila y ya.
     */
    suspend fun borraGasto(gastoId: String): String? {
        val gasto = bd.gastos().gasto(gastoId) ?: return null
        val compartido = bd.grupos().grupoDeUnaVez(gasto.grupoId)?.remotoId != null
        if (compartido) {
            bd.gastos().guarda(
                gasto.copy(borrado = true, version = System.currentTimeMillis())
            )
        } else {
            bd.gastos().borraPorId(gastoId)
        }
        return gasto.grupoId
    }

    /** Pulgar arriba/abajo de un colega a un gasto. Volver a votar lo quita. */
    suspend fun votaGasto(gastoId: String, colegaId: String, arriba: Boolean): String? {
        val gasto = bd.gastos().gasto(gastoId)?.aDominio() ?: return null
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
            },
            // Cada voto es un cambio: sube la version para que viaje al sincronizar.
            version = System.currentTimeMillis()
        )
        bd.gastos().guarda(actualizado.aEntidad())
        return gasto.grupoId
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
                nota = nota,
                version = System.currentTimeMillis()
            ).aEntidad()
        )
    }

    /** Igual que [borraGasto]: lapida si el grupo esta compartido. */
    suspend fun borraPago(pagoId: String): String? {
        val pago = bd.pagos().pagoPorId(pagoId) ?: return null
        val compartido = bd.grupos().grupoDeUnaVez(pago.grupoId)?.remotoId != null
        if (compartido) {
            bd.pagos().guarda(
                pago.copy(borrado = true, version = System.currentTimeMillis())
            )
        } else {
            bd.pagos().borraPorId(pagoId)
        }
        return pago.grupoId
    }

    /** Cambia el avatar del colega que soy yo, en todos los grupos a la vez. */
    suspend fun guardaMiAvatar(avatar: String) {
        val mios = colegasActuales().filter { it.soyYo }
        bd.grupos().guardaColegas(mios.map { it.copy(avatar = avatar) })
    }

    /**
     * Cambia el monigote de un colega. Si resulta que ese colega soy yo, se
     * cambia en todos los grupos: si no, quedarian dos "yo" con cara distinta y
     * la portada mostraria uno u otro sin criterio.
     */
    suspend fun guardaAvatarDe(colegaId: String, avatar: String) {
        val colega = colegasActuales().firstOrNull { it.id == colegaId } ?: return
        if (colega.soyYo) {
            guardaMiAvatar(avatar)
        } else {
            bd.grupos().guardaColegas(listOf(colega.copy(avatar = avatar)))
        }
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
