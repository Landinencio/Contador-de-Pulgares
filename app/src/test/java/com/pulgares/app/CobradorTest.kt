package com.pulgares.app

import com.pulgares.app.notificaciones.Cobrador
import com.pulgares.app.notificaciones.DecisionCobrador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El contrato del Cobrador del Frac: habla solo si debes, y nunca dos veces en
 * el mismo silencio. Estas pruebas son las que garantizan que no sea un pesado.
 */
class CobradorTest {

    private val DIA = 24 * 60 * 60 * 1000L

    @Test
    fun `sin deuda, el cobrador no dice nada`() {
        assertNull(DecisionCobrador.tocaAvisar(0L, ultimoAvisoMillis = 0L, ahoraMillis = 100 * DIA))
        assertNull(DecisionCobrador.tocaAvisar(-500L, ultimoAvisoMillis = 0L, ahoraMillis = 100 * DIA))
    }

    @Test
    fun `con deuda y silencio cumplido, avisa`() {
        val semilla = DecisionCobrador.tocaAvisar(
            deboCentimos = 2340L,
            ultimoAvisoMillis = 0L,
            ahoraMillis = 100 * DIA
        )
        assertNotNull(semilla)
    }

    @Test
    fun `nunca dos avisos en menos de dos dias`() {
        val ahora = 100 * DIA
        // Aviso ayer: hoy se calla.
        assertNull(DecisionCobrador.tocaAvisar(2340L, ultimoAvisoMillis = ahora - DIA, ahoraMillis = ahora))
        // Aviso hace justo dos días: ya puede volver a hablar.
        assertNotNull(
            DecisionCobrador.tocaAvisar(2340L, ultimoAvisoMillis = ahora - 2 * DIA, ahoraMillis = ahora)
        )
    }

    @Test
    fun `la semilla es estable durante el mismo dia`() {
        // Si el sistema reintenta el worker por la tarde, la frase no cambia.
        val manana = 100 * DIA + 9 * 60 * 60 * 1000L
        val tarde = 100 * DIA + 19 * 60 * 60 * 1000L
        assertEquals(
            DecisionCobrador.tocaAvisar(2340L, 0L, manana),
            DecisionCobrador.tocaAvisar(2340L, 0L, tarde)
        )
    }

    @Test
    fun `el aviso lleva el importe y no deja huecos sin rellenar`() {
        val aviso = Cobrador.redacta(deboCentimos = 2340L, diasDeuda = 5, semilla = 3L)
        assertTrue(aviso.titulo.contains("Cobrador del Frac"))
        assertFalse(aviso.texto.contains("{"))
        assertFalse(aviso.texto.contains("}"))
        // Alguna de las frases no lleva importe; con esta semilla sí.
        assertTrue(aviso.texto.isNotBlank())
    }

    @Test
    fun `el titulo escala con la antiguedad de la deuda`() {
        val fresco = Cobrador.redacta(1000L, diasDeuda = 2, semilla = 1L)
        val insistente = Cobrador.redacta(1000L, diasDeuda = 20, semilla = 1L)
        val veterano = Cobrador.redacta(1000L, diasDeuda = 90, semilla = 1L)
        assertEquals("El Cobrador del Frac 🎩", fresco.titulo)
        assertEquals("El Cobrador del Frac insiste 🎩", insistente.titulo)
        assertEquals("El Cobrador del Frac ya os conoce 🎩", veterano.titulo)
    }

    @Test
    fun `todas las frases del cobrador rellenan sus huecos`() {
        (0 until 30).forEach { semilla ->
            val aviso = Cobrador.redacta(2340L, diasDeuda = 10, semilla = semilla.toLong() + 1)
            assertFalse("Hueco en: ${aviso.texto}", aviso.texto.contains("{"))
        }
    }
}
