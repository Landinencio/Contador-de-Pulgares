package com.pulgares.app.domain.model

import kotlin.math.abs

/**
 * Todo el dinero de la app son centimos en Long. Nunca Double: con floats,
 * 0,10 + 0,20 no es 0,30 y las cuentas entre colegas acaban con un descuadre
 * de un centimo que nadie sabe explicar.
 */
object Dinero {

    /**
     * Cuantos digitos enteros se admiten. Nueve dan hasta 999.999.999 €, que
     * cubre cualquier cena imaginable y deja de sobra para que ni la conversion a
     * pesetas ni el reparto proporcional desborden el Long.
     */
    const val MAX_DIGITOS = 9

    /**
     * "12,50" o "12.50" o "12" -> 1250 centimos. Devuelve null si no cuela.
     *
     * Tambien devuelve null si el numero es absurdamente grande: sin ese tope,
     * teclear veinte digitos daba la vuelta al Long y se guardaba un importe que
     * no tenia nada que ver con lo escrito.
     */
    fun parse(texto: String): Long? {
        val limpio = texto.trim()
            .replace("€", "")
            .replace(" ", "")
            .replace(".", ",")
        if (limpio.isEmpty()) return null

        val partes = limpio.split(",")
        if (partes.size > 2) return null

        val enteros = partes[0].ifEmpty { "0" }
        if (!enteros.all { it.isDigit() }) return null
        if (enteros.trimStart('0').length > MAX_DIGITOS) return null

        val decimales = if (partes.size == 2) partes[1] else ""
        if (!decimales.all { it.isDigit() }) return null

        val centimos = when {
            decimales.isEmpty() -> "00"
            decimales.length == 1 -> decimales + "0"
            decimales.length == 2 -> decimales
            else -> return null
        }

        val total = enteros.toLongOrNull() ?: return null
        return total * 100 + centimos.toLong()
    }

    /** 1250 -> "12,50 €". Los negativos salen con el signo delante. */
    fun formatea(centimos: Long, conSimbolo: Boolean = true): String {
        val signo = if (centimos < 0) "-" else ""
        val abs = abs(centimos)
        val euros = abs / 100
        val resto = abs % 100
        val simbolo = if (conSimbolo) " €" else ""
        return "$signo$euros,${resto.toString().padStart(2, '0')}$simbolo"
    }

    /**
     * El cambio fijo e irrevocable de 1998: 1 € = 166,386 pesetas. No se toca,
     * es el oficial y con el que discutia media España.
     */
    const val PESETAS_POR_EURO = 166.386

    /**
     * Centimos de euro a pesetas enteras, redondeando al alza desde la mitad.
     * Se hace con enteros (166386 milesimas) para no arrastrar decimales.
     */
    fun aPesetas(centimos: Long): Long {
        val signo = if (centimos < 0) -1L else 1L
        // Tope para que la multiplicacion no de la vuelta al Long y salgan
        // pesetas negativas debajo de euros positivos. Se descuenta el 50.000 del
        // redondeo, que tambien tiene que caber.
        val techo = (Long.MAX_VALUE - 50_000L) / 166_386L
        val valorAbsoluto = abs(centimos).coerceAtMost(techo)
        return signo * ((valorAbsoluto * 166_386L + 50_000L) / 100_000L)
    }

    /** 2340 -> "3.893 pts". El guiño para el grupo de siempre. */
    fun formateaPesetas(centimos: Long): String {
        val pesetas = aPesetas(centimos)
        val signo = if (pesetas < 0) "-" else ""
        return "$signo${agrupaMiles(abs(pesetas))} pts"
    }

    /** "23,40 € · 3.893 pts", para cuando caben las dos en una linea. */
    fun formateaConPesetas(centimos: Long): String =
        "${formatea(centimos)} · ${formateaPesetas(centimos)}"

    /** Version corta para cabeceras: 1250 -> "12,50", 150000 -> "1.500". */
    fun formateaCorto(centimos: Long): String {
        val abs = abs(centimos)
        val signo = if (centimos < 0) "-" else ""
        return if (abs % 100 == 0L) {
            val euros = abs / 100
            "$signo${agrupaMiles(euros)} €"
        } else {
            formatea(centimos)
        }
    }

    private fun agrupaMiles(valor: Long): String {
        val texto = valor.toString()
        if (texto.length <= 3) return texto
        return texto.reversed().chunked(3).joinToString(".").reversed()
    }

    /**
     * Reparte [total] centimos entre [cuantos] partes de forma exacta: la suma
     * de las partes es SIEMPRE el total. Los centimos que sobran (total % n) se
     * dan a las primeras partes, rotando desde [desde] para que el marron del
     * centimo extra no le toque siempre al mismo pringado.
     */
    fun reparte(total: Long, cuantos: Int, desde: Int = 0): List<Long> {
        require(cuantos > 0) { "No se puede repartir entre cero personas" }
        val base = total / cuantos
        val sobran = (total % cuantos).toInt()
        val rotacion = ((desde % cuantos) + cuantos) % cuantos
        return List(cuantos) { indice ->
            // La posicion relativa respecto al arranque de la rotacion decide
            // si a esta parte le cae uno de los centimos sueltos.
            val posicion = ((indice - rotacion) + cuantos) % cuantos
            if (posicion < abs(sobran)) {
                if (sobran > 0) base + 1 else base - 1
            } else {
                base
            }
        }
    }

    /**
     * Reparte [total] proporcionalmente a [pesos], sin perder ni inventar
     * centimos: se reparte por parte entera y el resto va a quien mas decimal
     * arrastraba (metodo del resto mayor, el de los escanos).
     */
    fun reparteProporcional(total: Long, pesos: List<Int>): List<Long> {
        require(pesos.isNotEmpty()) { "Hacen falta pesos para repartir" }
        val suma = pesos.sumOf { it.toLong() }
        require(suma > 0) { "Los pesos no pueden sumar cero" }

        val brutos = pesos.map { peso -> total * peso }
        val enteros = brutos.map { it / suma }.toMutableList()
        var repartido = enteros.sum()

        // Ordena por resto descendente para dar los centimos que faltan. Con
        // total negativo la division entera trunca hacia cero, asi que lo que
        // falta va en la otra direccion: el paso es +1 o -1 segun el signo.
        val paso = if (total < 0) -1L else 1L
        val porResto = brutos.indices.sortedByDescending { abs(brutos[it] % suma) }
        var i = 0
        while (repartido != total && porResto.isNotEmpty()) {
            enteros[porResto[i % porResto.size]] += paso
            repartido += paso
            i += 1
        }
        return enteros
    }
}
