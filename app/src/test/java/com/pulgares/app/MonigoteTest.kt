package com.pulgares.app

import com.pulgares.app.avatar.Dimension
import com.pulgares.app.avatar.Monigote
import com.pulgares.app.avatar.TOTAL_SILUETAS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MonigoteTest {

    @Test
    fun `serializa y vuelve igual`() {
        val original = Monigote(
            forma = 3, color = 7, ojos = 5, boca = 11, pelo = 4, tocado = 9,
            gafas = 2, barba = 6, accesorio = 13, marca = 8, fondo = 12
        )
        assertEquals(original, Monigote.parse(original.serializa()))
    }

    @Test
    fun `descarta basura y versiones desconocidas`() {
        assertNull(Monigote.parse(null))
        assertNull(Monigote.parse(""))
        assertNull(Monigote.parse("v1:1,2,3"))
        assertNull(Monigote.parse("m1:1,2,3"))
        assertNull(Monigote.parse("cualquier cosa"))
    }

    @Test
    fun `un indice fuera de rango se recorta en vez de reventar`() {
        val leido = Monigote.parse("m1:99,99,99,99,99,99,99,99,99,99,99")
        assertTrue(leido != null)
        requireNotNull(leido)
        Dimension.entries.forEach { d ->
            assertTrue(
                "${d.name} fuera de rango: ${leido.valorDe(d)}",
                leido.valorDe(d) in 0 until d.cuantos
            )
        }
    }

    @Test
    fun `cambiar una dimension da la vuelta al llegar al final`() {
        val partida = Monigote(pelo = 0)
        // Hacia atras desde 0 lleva a la ultima variante.
        assertEquals(Dimension.PELO.cuantos - 1, partida.cambia(Dimension.PELO, -1).pelo)
        // Y una vuelta completa devuelve al mismo sitio.
        var vuelta = partida
        repeat(Dimension.PELO.cuantos) { vuelta = vuelta.cambia(Dimension.PELO, 1) }
        assertEquals(partida.pelo, vuelta.pelo)
    }

    @Test
    fun `todos los catalogos tienen nombre para cada variante`() {
        Dimension.entries.forEach { d ->
            assertTrue("${d.name} sin variantes", d.cuantos > 0)
            d.nombres.forEach { nombre ->
                assertTrue("Nombre vacio en ${d.name}", nombre.isNotBlank())
            }
            // Sin nombres repetidos dentro de una misma dimension.
            assertEquals("${d.name} tiene nombres repetidos", d.cuantos, d.nombres.distinct().size)
        }
    }

    @Test
    fun `hay una silueta dibujada por cada forma del catalogo`() {
        assertEquals(Dimension.FORMA.cuantos, TOTAL_SILUETAS)
    }

    @Test
    fun `la misma semilla da el mismo monigote`() {
        assertEquals(Monigote.desdeSemilla("Ana"), Monigote.desdeSemilla("Ana"))
        assertNotEquals(Monigote.desdeSemilla("Ana"), Monigote.desdeSemilla("Luis"))
    }

    @Test
    fun `los aleatorios siempre salen dentro de rango`() {
        val random = Random(1234)
        repeat(300) {
            val bicho = Monigote.aleatorio(random)
            Dimension.entries.forEach { d ->
                assertTrue(
                    "${d.name} fuera de rango: ${bicho.valorDe(d)}",
                    bicho.valorDe(d) in 0 until d.cuantos
                )
            }
            // Y siempre se puede guardar y recuperar.
            assertEquals(bicho, Monigote.parse(bicho.serializa()))
        }
    }

    @Test
    fun `hay millones de monigotes posibles`() {
        // El numero que se presume en Ajustes: si se anaden variantes, sube.
        val esperado = Dimension.entries.fold(1L) { acc, d -> acc * d.cuantos }
        assertEquals(esperado, Monigote.combinaciones)
        assertTrue("Pocas combinaciones: ${Monigote.combinaciones}", Monigote.combinaciones > 1_000_000_000L)
    }
}
