package com.pulgares.app.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.pulgares.app.data.Repositorio
import com.pulgares.app.data.RepositorioNube
import com.pulgares.app.data.local.BaseDatos
import com.pulgares.app.data.red.IdentidadMovil
import java.util.concurrent.TimeUnit

/**
 * La ronda del Cobrador del Frac: una vez al día mira si debes algo y, si toca
 * (ver [DecisionCobrador]), lo recuerda con una notificación. Antes de mirar,
 * sincroniza en silencio los grupos compartidos, así el cobrador se entera de
 * los gastos que apuntaron los demás aunque no abras la app.
 */
class CobradorWorker(
    context: Context,
    parametros: WorkerParameters
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        val identidad = IdentidadMovil(applicationContext)
        if (!identidad.cobradorContratado()) return Result.success()

        val bd = BaseDatos.obten(applicationContext)
        val repo = Repositorio(bd)
        val nube = RepositorioNube(bd, repo, identidad)

        // La ronda por la nube: en silencio y sin dramas si no hay cobertura.
        // Si en la sincronización caen zumbidos, se notifican aquí mismo: la
        // nube los entrega una sola vez, así que o se enseñan ahora o se pierden.
        if (nube.disponible) {
            bd.grupos().gruposDeUnaVez()
                .filter { it.remotoId != null }
                .forEach { grupo ->
                    runCatching { nube.sincroniza(grupo.id) }.onSuccess { resultado ->
                        resultado.zumbidos.forEach { zumbido ->
                            val quien = repo.grupoDeUnaVez(grupo.id)?.grupo
                                ?.colega(zumbido.deColegaId)?.nombre ?: "Alguien"
                            Zumbador.zumba(applicationContext)
                            Cobrador.notifica(
                                applicationContext,
                                Cobrador.Aviso(
                                    titulo = "¡ZUMBIDO! 🐝 " +
                                        com.pulgares.app.frases.Frases
                                            .chapaZumbido(zumbido.veces),
                                    texto = com.pulgares.app.frases.Frases.para(
                                        com.pulgares.app.frases.Momento.ZUMBIDO,
                                        quien = quien,
                                        semilla = zumbido.creadoMillis
                                    )
                                )
                            )
                        }
                    }
                }
        }

        // ¿Cuánto debo, sumando todos los grupos, y desde hace cuánto?
        var debo = 0L
        var dias = 0
        val ahora = System.currentTimeMillis()
        for (grupo in bd.grupos().gruposDeUnaVez()) {
            val estado = repo.grupoDeUnaVez(grupo.id) ?: continue
            val mio = estado.miSituacion
            if (mio.deboCentimos > 0) {
                debo += mio.deboCentimos
                estado.grupo.yo?.id?.let { yoId ->
                    dias = maxOf(dias, estado.diasDeudaDe(yoId, ahora))
                }
            }
        }

        val semilla = DecisionCobrador.tocaAvisar(debo, identidad.ultimoAvisoCobrador(), ahora)
        if (semilla != null) {
            Cobrador.notifica(applicationContext, Cobrador.redacta(debo, dias, semilla))
            identidad.marcaAvisoCobrador(ahora)
        }
        return Result.success()
    }

    companion object {
        private const val RONDA = "cobrador"

        /** Contrata al cobrador: una ronda diaria, más una inmediata de saludo. */
        fun contrata(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                RONDA,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<CobradorWorker>(1, TimeUnit.DAYS).build()
            )
            // La primera ronda, ya: si debes algo, el saludo llega en un minuto
            // y se ve que el señor del frac existe.
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<CobradorWorker>().build()
            )
        }

        fun despide(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(RONDA)
        }

        /**
         * Asegura la ronda diaria sin el saludo inmediato. Se llama en cada
         * arranque: como el cobrador viene contratado de fábrica, alguien tiene
         * que dejar la ronda programada aunque nadie pulsara "contratar".
         */
        fun asegura(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                RONDA,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<CobradorWorker>(1, TimeUnit.DAYS).build()
            )
        }
    }
}
