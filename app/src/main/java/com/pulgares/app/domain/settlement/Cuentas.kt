package com.pulgares.app.domain.settlement

import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Pago

/**
 * El saldo de un colega en un grupo.
 *
 * [neto] positivo = puso mas de lo que le tocaba, el grupo le debe.
 * [neto] negativo = debe pasar por caja.
 */
data class Saldo(
    val colegaId: String,
    val pagadoCentimos: Long,
    val debidoCentimos: Long
) {
    val neto: Long get() = pagadoCentimos - debidoCentimos
    val esAcreedor: Boolean get() = neto > 0
    val esDeudor: Boolean get() = neto < 0
    val enPaz: Boolean get() = neto == 0L
}

/** Un movimiento del plan: [deQuienId] le pasa [importeCentimos] a [aQuienId]. */
data class Transferencia(
    val deQuienId: String,
    val aQuienId: String,
    val importeCentimos: Long
)

/**
 * Las cuentas del grupo. Todo se calcula en centimos enteros, asi que los
 * netos suman exactamente cero y el plan de pagos deja a todo el mundo a cero.
 */
object Cuentas {

    /**
     * Saldo de cada colega. Un [Pago] cuenta como "pagado" para quien lo hace y
     * como "debido" para quien lo recibe: mover dinero salda deuda, no crea gasto.
     */
    fun saldos(
        colegas: List<Colega>,
        gastos: List<Gasto>,
        pagos: List<Pago> = emptyList()
    ): List<Saldo> {
        val pagado = mutableMapOf<String, Long>()
        val debido = mutableMapOf<String, Long>()

        for (gasto in gastos) {
            pagado[gasto.pagadorId] = (pagado[gasto.pagadorId] ?: 0L) + gasto.importeCentimos
            for ((colegaId, cuanto) in gasto.deudas()) {
                debido[colegaId] = (debido[colegaId] ?: 0L) + cuanto
            }
        }

        for (pago in pagos) {
            pagado[pago.deQuienId] = (pagado[pago.deQuienId] ?: 0L) + pago.importeCentimos
            debido[pago.aQuienId] = (debido[pago.aQuienId] ?: 0L) + pago.importeCentimos
        }

        // Se listan todos los colegas del grupo, tambien los que no aparecen en
        // ningun gasto (saldo cero), e ids huerfanos de gastos ya editados.
        val ids = LinkedHashSet<String>().apply {
            addAll(colegas.map { it.id })
            addAll(pagado.keys)
            addAll(debido.keys)
        }

        return ids.map { id ->
            Saldo(
                colegaId = id,
                pagadoCentimos = pagado[id] ?: 0L,
                debidoCentimos = debido[id] ?: 0L
            )
        }
    }

    /**
     * Plan de pagos con el minimo razonable de transferencias: se empareja al
     * que mas debe con el que mas se le debe, se salda lo que se pueda y se
     * repite. Es el algoritmo greedy clasico de Splitwise y compania: no
     * garantiza el optimo teorico (es NP-duro) pero deja como maximo N-1
     * bizums, y en la practica el minimo casi siempre.
     *
     * Los empates se rompen por id para que el plan no baile entre pantallas.
     */
    fun planDePagos(saldos: List<Saldo>): List<Transferencia> {
        val deudores = saldos.filter { it.esDeudor }
            .map { it.colegaId to -it.neto }
            .sortedWith(compareByDescending<Pair<String, Long>> { it.second }.thenBy { it.first })
            .toMutableList()

        val acreedores = saldos.filter { it.esAcreedor }
            .map { it.colegaId to it.neto }
            .sortedWith(compareByDescending<Pair<String, Long>> { it.second }.thenBy { it.first })
            .toMutableList()

        val plan = mutableListOf<Transferencia>()

        while (deudores.isNotEmpty() && acreedores.isNotEmpty()) {
            val (deudorId, debe) = deudores.first()
            val (acreedorId, leDeben) = acreedores.first()
            val importe = minOf(debe, leDeben)

            if (importe > 0) {
                plan += Transferencia(deudorId, acreedorId, importe)
            }

            val restaDeudor = debe - importe
            val restaAcreedor = leDeben - importe

            deudores.removeAt(0)
            acreedores.removeAt(0)
            if (restaDeudor > 0) insertaOrdenado(deudores, deudorId to restaDeudor)
            if (restaAcreedor > 0) insertaOrdenado(acreedores, acreedorId to restaAcreedor)
        }

        return plan
    }

    /** Reinserta manteniendo el orden (importe desc, id asc). */
    private fun insertaOrdenado(
        lista: MutableList<Pair<String, Long>>,
        elemento: Pair<String, Long>
    ) {
        val posicion = lista.indexOfFirst { actual ->
            actual.second < elemento.second ||
                (actual.second == elemento.second && actual.first > elemento.first)
        }
        if (posicion < 0) lista.add(elemento) else lista.add(posicion, elemento)
    }

    /**
     * Lo que a [miId] le toca hacer o cobrar segun el plan, que es lo unico que
     * de verdad le importa al dueno del movil.
     */
    fun loMioDelPlan(plan: List<Transferencia>, miId: String): MiSituacion {
        val debo = plan.filter { it.deQuienId == miId }
        val meDeben = plan.filter { it.aQuienId == miId }
        return MiSituacion(
            deboCentimos = debo.sumOf { it.importeCentimos },
            meDebenCentimos = meDeben.sumOf { it.importeCentimos },
            aQuienDebo = debo,
            quienMeDebe = meDeben
        )
    }

    data class MiSituacion(
        val deboCentimos: Long,
        val meDebenCentimos: Long,
        val aQuienDebo: List<Transferencia>,
        val quienMeDebe: List<Transferencia>
    ) {
        val neto: Long get() = meDebenCentimos - deboCentimos
        val enPaz: Boolean get() = deboCentimos == 0L && meDebenCentimos == 0L
    }

    /** Total movido en el grupo (solo gastos, los bizums no son gasto). */
    fun totalGastado(gastos: List<Gasto>): Long = gastos.sumOf { it.importeCentimos }
}
