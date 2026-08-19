package com.pulgares.app

import com.pulgares.app.frases.Chascarrillos
import com.pulgares.app.frases.Frases
import com.pulgares.app.frases.Momento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun `el nivel de zumbido sube uno cada tres y no se acaba nunca`() {
        // Tres zumbidos = un nivel. Ni dos ni cuatro: los testers se quejaron
        // de llegar a "acreedor" con tres toques.
        assertEquals(listOf(1, 1, 1, 2, 2, 2, 3), (1..7).map { Frases.nivelZumbido(it) })
        // Zumbar una vez ya tiene rango, y el mas barato de la escalera.
        assertEquals("Toque de cortesía", Frases.rangoZumbido(1))
        // "Acreedor" ya no se regala: hace falta pasar de la docena.
        assertEquals("Acreedor", Frases.rangoZumbido(13))
        assertEquals(5, Frases.nivelZumbido(13))

        // 28 rangos distintos, uno cada tres zumbidos: 84 zumbidos de escalera.
        val rangos = (1..84 step 3).map { Frases.rangoZumbido(it) }
        assertEquals(28, rangos.size)
        assertEquals(rangos.size, rangos.distinct().size)
        rangos.forEach { assertTrue(it.isNotBlank()) }

        // Pasado el ultimo rango no revienta: se queda en el techo y el numero
        // de nivel sigue subiendo (el que zumbe 1.000 veces se lo ha ganado).
        assertEquals("Deidad del zumbido", Frases.rangoZumbido(84))
        assertEquals(Frases.rangoZumbido(84), Frases.rangoZumbido(1_000))
        assertNotEquals(Frases.rangoZumbido(81), Frases.rangoZumbido(84))
        assertEquals(334, Frases.nivelZumbido(1_000))

        // La chapa del carteloÌn: con una vez no pone "×1", con varias si.
        assertEquals("nivel 1 — Toque de cortesía", Frases.chapaZumbido(1))
        assertEquals("×13 · nivel 5 — Acreedor", Frases.chapaZumbido(13))
    }

    @Test
    fun `el sermon al zumbador cambia con el dia del mes`() {
        // Con pocos zumbidos no hay sermon: al primer toque nadie necesita padre.
        assertNull(Frases.sermonZumbador(1, 5))
        assertNull(Frases.sermonZumbador(3, 5))
        assertNotNull(Frases.sermonZumbador(4, 5))

        // Los tres tramos del mes dan sermones distintos.
        val principio = Frases.sermonZumbador(6, 3, semilla = 0)
        val mitad = Frases.sermonZumbador(6, 15, semilla = 0)
        val finales = Frases.sermonZumbador(6, 28, semilla = 0)
        assertEquals(3, setOf(principio, mitad, finales).size)

        // Dentro de un tramo, el sermon es el mismo todos los dias; al cruzar la
        // frontera (dia 10 y dia 20) cambia. Ahi estan los limites de verdad.
        val delTramo1 = Frases.sermonZumbador(9, 1, semilla = 2)
        assertTrue((1..10).all { Frases.sermonZumbador(9, it, semilla = 2) == delTramo1 })
        assertNotEquals(delTramo1, Frases.sermonZumbador(9, 11, semilla = 2))

        val delTramo2 = Frases.sermonZumbador(9, 11, semilla = 2)
        assertTrue((11..20).all { Frases.sermonZumbador(9, it, semilla = 2) == delTramo2 })
        assertNotEquals(delTramo2, Frases.sermonZumbador(9, 21, semilla = 2))

        val delTramo3 = Frases.sermonZumbador(9, 21, semilla = 2)
        assertTrue((21..31).all { Frases.sermonZumbador(9, it, semilla = 2) == delTramo3 })

        // Con semilla no baila; ese fue un bicho real de las frases.
        assertEquals(finales, Frases.sermonZumbador(9, 28, semilla = 0))
    }

    @Test
    fun `los avisos de renombrado dicen el nombre nuevo`() {
        val grupo = Frases.para(Momento.NOMBRE_GRUPO, que = "Los pagafantas", semilla = 3)
        assertTrue(grupo.contains("Los pagafantas"))

        val colega = Frases.para(
            Momento.NOMBRE_COLEGA,
            quien = "Berto",
            que = "El Moroso",
            semilla = 3
        )
        assertTrue(colega.contains("Berto"))
        assertTrue(colega.contains("El Moroso"))

        // Ninguna plantilla deja un hueco sin rellenar: eso se veria en pantalla.
        for (semilla in 0L..40L) {
            for (momento in listOf(
                Momento.NOMBRE_GRUPO, Momento.NOMBRE_COLEGA, Momento.NOMBRE_A_VOTACION
            )) {
                val frase = Frases.para(momento, quien = "Ana", que = "Nuevo", semilla = semilla)
                assertFalse("$momento deja huecos: $frase", frase.contains("{"))
            }
        }
    }

    @Test
    fun `el fiscal salta con los conceptos turbios y calla con los honrados`() {
        // Lo que pidieron los testers, tal cual.
        assertEquals("Ábalos estaría orgulloso.", Chascarrillos.para("Travestis", semilla = 0))
        assertEquals(
            "¿Qué jugador se perdió la Gürtel contigo?",
            Chascarrillos.para("2 gramos de coca", semilla = 0)
        )

        // Un gasto honrado no lleva chapa.
        assertNull(Chascarrillos.para("Cañas del viernes"))
        assertNull(Chascarrillos.para("Alquiler"))
        assertNull(Chascarrillos.para(""))

        // "cocacola" NO es "coca": el falso positivo mas obvio de la lista.
        assertNull(Chascarrillos.para("Coca-Cola"))
        assertNull(Chascarrillos.para("coca cola zero"))
        assertNull(Chascarrillos.para("cocacolas"))
        assertNull(Chascarrillos.para("Club de lectura"))

        // Las claves se buscan como PALABRA ENTERA, no como trozo de otra. Sin
        // eso, "queso" contiene "eso" y un queso manchego salia acusado de
        // contabilidad opaca. Estos cuatro no los salvan las excepciones: los
        // salva el buscar palabras completas.
        assertNull(Chascarrillos.para("Queso manchego"))
        assertNull(Chascarrillos.para("Crema de manos"))
        assertNull(Chascarrillos.para("Bollos del obrador"))
        assertNull(Chascarrillos.para("Cristalera del salon"))

        // Tildes, mayusculas y signos no despistan al fiscal.
        assertNotNull(Chascarrillos.para("¡MARISCADA!"))
        assertNotNull(Chascarrillos.para("Reforma del baño"))
        assertNotNull(Chascarrillos.para("pagado en efectivo"))
        assertNotNull(Chascarrillos.para("cuenta en Andorra"))
        assertNotNull(Chascarrillos.para("varios"))

        // Con semilla la frase es siempre la misma (si no, bailaria en pantalla).
        val unaVez = Chascarrillos.para("obra del salon", semilla = 77)
        assertEquals(unaVez, Chascarrillos.para("obra del salon", semilla = 77))
        assertNotNull(unaVez)

        // Semillas negativas (hashCode de un id) no revientan.
        assertNotNull(Chascarrillos.para("yate", semilla = -999_999))
    }
}
