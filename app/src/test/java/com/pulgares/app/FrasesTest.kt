package com.pulgares.app

import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FrasesTest {

    @Test
    fun `ningun momento se queda sin frases`() {
        Momento.entries.forEach { momento ->
            val frase = Frases.para(momento, quien = "Ana", cuanto = "12,50 €", que = "birras", dias = 5)
            assertTrue("$momento sin frase", frase.isNotBlank())
        }
    }

    @Test
    fun `no queda ningun hueco sin rellenar`() {
        // Recorre todas las frases de todos los momentos (la semilla las barre
        // en orden) y comprueba que no hay typos tipo {quién} o {Cuanto}.
        Momento.entries.forEach { momento ->
            (0 until 40).forEach { i ->
                val frase = Frases.para(
                    momento,
                    quien = "Ana",
                    cuanto = "12,50 €",
                    que = "birras",
                    dias = 7,
                    semilla = i.toLong() + 1
                )
                assertFalse("Hueco sin rellenar en $momento: $frase", frase.contains("{"))
                assertFalse("Hueco sin rellenar en $momento: $frase", frase.contains("}"))
            }
        }
    }

    @Test
    fun `la misma semilla da siempre la misma frase`() {
        val primera = Frases.para(Momento.DEBES, quien = "Luis", cuanto = "5,00 €", semilla = 42)
        val segunda = Frases.para(Momento.DEBES, quien = "Luis", cuanto = "5,00 €", semilla = 42)
        assertEquals(primera, segunda)
    }

    @Test
    fun `hay coña de sobra para no repetirse`() {
        assertTrue("Faltan frases: ${Frases.total}", Frases.total >= 150)
    }

    @Test
    fun `sin datos la frase sigue teniendo sentido`() {
        val frase = Frases.para(Momento.DEBES, semilla = 3)
        assertFalse(frase.contains("{"))
        assertTrue(frase.isNotBlank())
    }

    @Test
    fun `los rangos de moroso escalan con el tiempo`() {
        assertEquals("Intachable", Frases.rangoMoroso(0, 999))
        assertEquals("Despistado", Frases.rangoMoroso(500, 1))
        assertEquals("Moroso Patrimonio de la Humanidad", Frases.rangoMoroso(500, 400))
        // Cada tramo tiene nombre propio: nunca se repite el rango al subir.
        val rangos = listOf(1, 5, 10, 20, 45, 90, 200, 400).map { Frases.rangoMoroso(500, it) }
        assertEquals(rangos.size, rangos.distinct().size)
    }

    @Test
    fun `los rangos de pagador reconocen al pagafantas`() {
        assertEquals("Turista", Frases.rangoPagador(0, 0))
        assertEquals("Pagafantas oficial", Frases.rangoPagador(5000, 4))
        assertEquals("Cajero automático humano", Frases.rangoPagador(50000, 15))
    }

    @Test
    fun `las medallas de pulgares solo salen en los extremos`() {
        assertNotNull(Frases.medallaPulgares(6))
        assertNotNull(Frases.medallaPulgares(-6))
        assertEquals(null, Frases.medallaPulgares(1))
        assertEquals(null, Frases.medallaPulgares(-2))
    }
}
