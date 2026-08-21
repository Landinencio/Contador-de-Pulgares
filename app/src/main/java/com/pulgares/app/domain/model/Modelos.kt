package com.pulgares.app.domain.model

/**
 * Modelo de dominio del Contador de Pulgares. Un grupo (un viaje, un piso, las
 * cenas de los viernes) tiene colegas y gastos; de ahi salen los balances y el
 * plan de pagos.
 */

/**
 * Un colega del grupo. El avatar se guarda serializado (ver Monigote).
 *
 * Cuando alguien deja el grupo NO se borra su fila: se marca [activo] a false.
 * Si se borrara, sus gastos se quedarian con un id sin nombre y el plan de pagos
 * diria "Un fantasma debe 15 €"; y al editar uno de esos gastos, el reparto se
 * recalcularia sin el, cambiando cuentas que el grupo ya habia dado por buenas.
 */
data class Colega(
    val id: String,
    val nombre: String,
    val avatar: String? = null,
    /** Solo uno de los colegas es el dueno del movil. */
    val soyYo: Boolean = false,
    /** false = ya no esta en el grupo, pero sigue en los gastos de antes. */
    val activo: Boolean = true,
    /**
     * Millis del ultimo cambio (nombre, monigote o activo), para arbitrar al
     * sincronizar. Sin esto, cada subida reescribia los colegas con la hora
     * actual y el ultimo movil que sincronizaba pisaba el nombre que otro habia
     * cambiado: el cambio se veia un rato y volvia solo al de antes.
     */
    val version: Long = 0L
)

/** Como se parte un gasto entre los colegas. */
sealed interface Reparto {

    /** A escote: el importe entre todos los implicados, a partes iguales. */
    data class Escote(val entre: List<String>) : Reparto

    /**
     * Por partes: uno cuenta doble porque se comio dos platos, el que no bebio
     * cuenta media. Los pesos son enteros (2 = doble que un 1).
     */
    data class PorPartes(val pesos: Map<String, Int>) : Reparto

    /** A dedo: cada uno pone exactamente lo suyo. Debe sumar el importe. */
    data class Exacto(val importes: Map<String, Long>) : Reparto

    /**
     * Los ids implicados, sea cual sea el modo, y sin repetidos: un id duplicado
     * en un escote hacia que su parte se contase una sola vez y el resto del
     * dinero se evaporase, dejando los saldos del grupo sin sumar cero.
     */
    val implicados: List<String>
        get() = when (this) {
            is Escote -> entre.distinct()
            is PorPartes -> pesos.keys.toList()
            is Exacto -> importes.keys.toList()
        }
}

/** Categoria del gasto. El emoji es lo que se ve en la lista. */
enum class Categoria(val etiqueta: String, val emoji: String) {
    BIRRAS("Birras", "🍻"),
    COMIDA("Comida", "🍕"),
    CASA("Casa", "🏠"),
    VIAJE("Viaje", "✈️"),
    TAXI("Taxi", "🚖"),
    FIESTA("Fiesta", "🎉"),
    COMPRA("Compra", "🛒"),
    REGALO("Regalo", "🎁"),
    RESACA("Resaca", "🤬"),
    MISTERIO("Vete tu a saber", "🤷");

    companion object {
        fun porNombre(nombre: String?): Categoria =
            entries.firstOrNull { it.name == nombre } ?: MISTERIO
    }
}

/**
 * Un gasto: alguien puso el dinero y hay que repartirlo. Los pulgares son las
 * votaciones de los colegas al gasto (de ahi el nombre de la app).
 */
data class Gasto(
    val id: String,
    val grupoId: String,
    val concepto: String,
    val importeCentimos: Long,
    /** Quien saco la tarjeta. */
    val pagadorId: String,
    val fechaMillis: Long,
    val categoria: Categoria = Categoria.MISTERIO,
    val reparto: Reparto,
    val nota: String? = null,
    /** Ids de quien puso pulgar arriba y pulgar abajo. */
    val pulgaresArriba: Set<String> = emptySet(),
    val pulgaresAbajo: Set<String> = emptySet(),
    /**
     * Millis del ultimo cambio. Solo sirve para sincronizar entre moviles: gana
     * la version mas alta, asi que una copia vieja que llega tarde no pisa una
     * edicion mas nueva. En un solo movil da igual.
     */
    val version: Long = 0L,
    /**
     * Lapida. En un grupo compartido, borrar no puede ser borrar la fila: el
     * otro movil la devolveria en la siguiente sincronizacion, resucitando el
     * gasto. Se marca y se oculta, y la marca viaja igual que una edicion.
     */
    val borrado: Boolean = false
) {
    val saldoPulgares: Int get() = pulgaresArriba.size - pulgaresAbajo.size

    /** Cuanto le toca a cada colega en este gasto concreto. */
    fun deudas(): Map<String, Long> = when (val r = reparto) {
        is Reparto.Escote -> {
            val ids = r.entre.distinct()
            if (ids.isEmpty()) {
                emptyMap()
            } else {
                // La rotacion del centimo suelto depende del id del gasto, asi
                // que es estable para el mismo gasto pero varia entre gastos.
                val partes = Dinero.reparte(importeCentimos, ids.size, rotacion())
                ids.zip(partes).toMap()
            }
        }

        is Reparto.PorPartes -> {
            val ids = r.pesos.keys.toList()
            val pesos = ids.map { r.pesos[it] ?: 0 }
            if (ids.isEmpty() || pesos.sumOf { it.toLong() } <= 0L) {
                emptyMap()
            } else {
                ids.zip(Dinero.reparteProporcional(importeCentimos, pesos)).toMap()
            }
        }

        is Reparto.Exacto -> r.importes
    }

    /**
     * Las partes suman exactamente el importe. A escote y por partes cuadran
     * siempre por construccion; [Reparto.Exacto] depende de lo que teclee la
     * gente, y si no cuadra los saldos del grupo dejarian de sumar cero. La
     * pantalla de gasto no deja guardar un reparto descuadrado, y este flag
     * existe para que un gasto importado o migrado tampoco cuele.
     */
    val cuadra: Boolean get() = deudas().values.sum() == importeCentimos

    /** Semilla estable para rotar a quien le cae el centimo de mas. */
    private fun rotacion(): Int {
        val hash = id.hashCode()
        return if (hash == Int.MIN_VALUE) 0 else kotlin.math.abs(hash)
    }
}

/**
 * Un pago real entre dos colegas ("te hice un Bizum"). Se registra aparte de
 * los gastos: no es un gasto del grupo, es mover dinero para saldar cuentas.
 */
data class Pago(
    val id: String,
    val grupoId: String,
    val deQuienId: String,
    val aQuienId: String,
    val importeCentimos: Long,
    val fechaMillis: Long,
    val nota: String? = null,
    /** Ver [Gasto.version]. */
    val version: Long = 0L,
    /** Ver [Gasto.borrado]. */
    val borrado: Boolean = false
)

/** Un grupo de gente que se debe cosas. */
data class Grupo(
    val id: String,
    val nombre: String,
    val emoji: String = "👥",
    val colegas: List<Colega> = emptyList(),
    val creadoMillis: Long = 0L,
    /** Codigo corto para compartir el grupo, cuando esta compartido. */
    val codigo: String? = null,
    /** Id del grupo en la nube. null = solo vive en este movil. */
    val remotoId: String? = null,
    /** Millis del ultimo cambio de nombre o emoji (arbitraje al sincronizar). */
    val version: Long = 0L
) {
    /** ¿Este grupo lo comparten varios moviles? */
    val compartido: Boolean get() = remotoId != null

    fun colega(id: String): Colega? = colegas.firstOrNull { it.id == id }

    fun nombreDe(id: String): String = colega(id)?.nombre ?: "Un fantasma"

    val yo: Colega? get() = colegas.firstOrNull { it.soyYo }

    /** Los que siguen en el grupo: los unicos a los que se puede meter en un gasto nuevo. */
    val activos: List<Colega> get() = colegas.filter { it.activo }

    /** Los que se fueron pero siguen apareciendo en gastos viejos. */
    val salidos: List<Colega> get() = colegas.filterNot { it.activo }
}
