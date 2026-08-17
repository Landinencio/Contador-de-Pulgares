package com.pulgares.app.notificaciones

/**
 * Cuándo habla el Cobrador del Frac. Lógica pura, sin Android, para poder
 * probarla en JVM.
 *
 * La regla de la casa: el cobrador es elegante, no un pesado. Solo habla si de
 * verdad debes algo, y nunca más de una vez cada [SILENCIO_MS]. Estar a cero o
 * ser acreedor lo deja mudo.
 */
object DecisionCobrador {

    /** Dos días de silencio entre avisos: recordar sí, perseguir no. */
    const val SILENCIO_MS: Long = 2 * 24 * 60 * 60 * 1000L

    /**
     * Decide si toca aviso. Devuelve la semilla de la frase (estable por día,
     * para que reintentos del sistema el mismo día no cambien el texto) o null
     * si el cobrador se queda en su sillón.
     */
    fun tocaAvisar(
        deboCentimos: Long,
        ultimoAvisoMillis: Long,
        ahoraMillis: Long
    ): Long? {
        if (deboCentimos <= 0L) return null
        if (ahoraMillis - ultimoAvisoMillis < SILENCIO_MS) return null
        return ahoraMillis / 86_400_000L
    }
}
