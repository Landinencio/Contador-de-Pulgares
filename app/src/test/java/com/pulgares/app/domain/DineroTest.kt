package com.pulgares.app.domain

import com.pulgares.app.domain.model.Dinero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DineroTest {

    @Test
    fun `parsea euros con coma y con punto`() {
        assertEquals(1250L, Dinero.parse("12,50"))
        assertEquals(1250L, Dinero.parse("12.50"))
        assertEquals(1250L, Dinero.parse(" 12,50 € "))
        assertEquals(1200L, Dinero.parse("12"))
        assertEquals(1250L, Dinero.parse("12,5"))
        assertEquals(50L, Dinero.parse("0,50"))
        assertEquals(50L, Dinero.parse(",50"))
    }

    @Test
    fun `rechaza lo que no es dinero`() {
        assertNull(Dinero.parse(""))
        assertNull(Dinero.parse("doce"))
        assertNull(Dinero.parse("12,345"))
        assertNull(Dinero.parse("1,2,3"))
        assertNull(Dinero.parse("-5"))
    }

    @Test
    fun `formatea con dos decimales siempre`() {
        assertEquals("12,50 €", Dinero.formatea(1250))
        assertEquals("12,00 €", Dinero.formatea(1200))
        assertEquals("0,05 €", Dinero.formatea(5))
        assertEquals("-3,33 €", Dinero.formatea(-333))
        assertEquals("12,50", Dinero.formatea(1250, conSimbolo = false))
    }

    @Test
    fun `formato corto agrupa miles y quita decimales redondos`() {
        assertEquals("1.500 €", Dinero.formateaCorto(150000))
        assertEquals("12,34 €", Dinero.formateaCorto(1234))
        assertEquals("999 €", Dinero.formateaCorto(99900))
    }

    @Test
    fun `el reparto a escote no pierde ni inventa centimos`() {
        // 10 € entre 3 = 3,33 + 3,33 + 3,34
        val partes = Dinero.reparte(1000, 3)
        assertEquals(1000L, partes.sum())
        assertEquals(3, partes.size)
        assertEquals(listOf(334L, 333L, 333L), partes)
    }

    @Test
    fun `el centimo suelto rota segun la semilla`() {
        val primero = Dinero.reparte(1000, 3, desde = 0)
        val segundo = Dinero.reparte(1000, 3, desde = 1)
        val tercero = Dinero.reparte(1000, 3, desde = 2)

        // Siempre cuadra el total, pero el que paga el centimo de mas cambia.
        assertEquals(1000L, primero.sum())
        assertEquals(1000L, segundo.sum())
        assertEquals(1000L, tercero.sum())
        assertEquals(334L, primero[0])
        assertEquals(334L, segundo[1])
        assertEquals(334L, tercero[2])
    }

    @Test
    fun `reparto a escote con importes imposibles de dividir`() {
        val partes = Dinero.reparte(100, 7)
        assertEquals(100L, partes.sum())
        // 14,28... -> unos pagan 15 centimos y otros 14
        assertEquals(setOf(14L, 15L), partes.toSet())
    }

    @Test
    fun `reparto proporcional respeta pesos y cuadra el total`() {
        // 30 € entre uno que cuenta doble y dos normales: 15, 7,50, 7,50
        val partes = Dinero.reparteProporcional(3000, listOf(2, 1, 1))
        assertEquals(3000L, partes.sum())
        assertEquals(listOf(1500L, 750L, 750L), partes)
    }

    @Test
    fun `reparto proporcional con restos feos sigue cuadrando`() {
        val partes = Dinero.reparteProporcional(1000, listOf(1, 1, 1))
        assertEquals(1000L, partes.sum())

        val otros = Dinero.reparteProporcional(1001, listOf(3, 2, 1))
        assertEquals(1001L, otros.sum())
    }
}
