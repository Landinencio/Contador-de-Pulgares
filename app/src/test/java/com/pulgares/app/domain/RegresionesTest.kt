package com.pulgares.app.domain

import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Dinero
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Grupo
import com.pulgares.app.domain.model.Reparto
import com.pulgares.app.domain.settlement.Cuentas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Un test por cada fallo que ya se colo una vez. Si alguno de estos se pone rojo,
 * es que ha vuelto.
 */
class RegresionesTest {

    private val ruben = Colega("r", "Rubén", soyYo = true)
    private val ana = Colega("a", "Ana")
    private val luis = Colega("l", "Luis")

    @Test
    fun `un escote con el mismo id repetido no pierde dinero`() {
        // Con ids duplicados, zip se quedaba con una sola parte y el resto del
        // importe se evaporaba: los saldos dejaban de sumar cero.
        val gasto = Gasto(
            id = "1",
            grupoId = "g",
            concepto = "Duplicado",
            importeCentimos = 300,
            pagadorId = "r",
            fechaMillis = 0L,
            reparto = Reparto.Escote(listOf("a", "a", "b"))
        )
        assertEquals(300L, gasto.deudas().values.sum())
        assertTrue(gasto.cuadra)
        // Y con el id repetido tres veces se lo lleva entero uno solo.
        val todoUno = gasto.copy(reparto = Reparto.Escote(listOf("a", "a", "a")))
        assertEquals(300L, todoUno.deudas().values.sum())
        assertEquals(mapOf("a" to 300L), todoUno.deudas())
    }

    @Test
    fun `un importe absurdo se rechaza en vez de dar la vuelta al Long`() {
        // "200000000000000000,00" devolvia un numero positivo sin relacion con lo
        // teclado, y el gasto se guardaba con ese importe.
        assertNull(Dinero.parse("200000000000000000,00"))
        assertNull(Dinero.parse("9999999999,00"))
        // El tope son nueve digitos enteros: eso sigue valiendo.
        assertEquals(99_999_999_900L, Dinero.parse("999999999,00"))
        // Y los ceros de relleno no cuentan como digitos.
        assertEquals(1250L, Dinero.parse("0000012,50"))
    }

    @Test
    fun `las pesetas nunca salen negativas para un importe positivo`() {
        val tope = Dinero.parse("999999999,99") ?: error("deberia colar")
        assertTrue("Pesetas negativas con euros positivos", Dinero.aPesetas(tope) > 0)
        assertTrue(Dinero.formateaPesetas(tope).first() != '-')
        // Ni con el maximo que aguanta un Long.
        assertTrue(Dinero.aPesetas(Long.MAX_VALUE) > 0)
    }

    @Test
    fun `el reparto proporcional cuadra tambien con totales negativos`() {
        // La division entera trunca hacia cero, asi que con negativos faltaban
        // centimos y el bucle de ajuste no entraba nunca.
        assertEquals(-100L, Dinero.reparteProporcional(-100, listOf(3, 3, 3)).sum())
        assertEquals(-1001L, Dinero.reparteProporcional(-1001, listOf(3, 2, 1)).sum())
        assertEquals(-1L, Dinero.reparteProporcional(-1, listOf(1, 1, 1)).sum())
    }

    @Test
    fun `los colegas que se van siguen teniendo nombre en el grupo`() {
        // Antes se borraba su fila y el plan de pagos decia "Un fantasma".
        val grupo = Grupo(
            id = "g",
            nombre = "El piso",
            colegas = listOf(ruben, ana, luis.copy(activo = false))
        )
        assertEquals(listOf(ruben, ana), grupo.activos)
        assertEquals(listOf(luis.copy(activo = false)), grupo.salidos)
        assertEquals("Luis", grupo.nombreDe("l"))
        // Y su deuda sigue contando en las cuentas del grupo.
        val gastos = listOf(
            Gasto("1", "g", "Alquiler", 3000, "r", 0L, reparto = Reparto.Escote(listOf("r", "a", "l")))
        )
        val saldos = Cuentas.saldos(grupo.colegas, gastos)
        assertEquals(-1000L, saldos.first { it.colegaId == "l" }.neto)
        assertEquals(0L, saldos.sumOf { it.neto })
    }

    @Test
    fun `el centimo suelto depende del gasto y es estable`() {
        // La vista previa usaba un id inventado, asi que anunciaba un reparto y
        // guardaba otro. Con el mismo id, el reparto tiene que ser identico.
        val ids = listOf("r", "a", "l")
        val primero = Gasto("gasto-abc", "g", "", 1000, "r", 0L, reparto = Reparto.Escote(ids))
        val mismo = Gasto("gasto-abc", "g", "otro concepto", 1000, "a", 99L, reparto = Reparto.Escote(ids))
        assertEquals(primero.deudas(), mismo.deudas())

        // Y un gasto nuevo (id vacio, el que usa la vista previa) tambien cuadra.
        val nuevo = Gasto("", "g", "", 1000, "r", 0L, reparto = Reparto.Escote(ids))
        assertEquals(1000L, nuevo.deudas().values.sum())
    }

    @Test
    fun `editar un gasto conserva sus pulgares y su fecha`() {
        // Este era el peor: guardar un cambio de concepto borraba los votos y
        // fechaba el gasto hoy, lo que reiniciaba la antiguedad de la deuda.
        val original = Gasto(
            id = "gasto-1",
            grupoId = "g",
            concepto = "Cañas",
            importeCentimos = 3000,
            pagadorId = "r",
            fechaMillis = 1_600_000_000_000L,
            reparto = Reparto.Escote(listOf("r", "a", "l")),
            pulgaresArriba = setOf("a", "l"),
            pulgaresAbajo = setOf("p")
        )
        // Lo que hace el ViewModel al editar: mismo id, fecha y pulgares.
        val editado = original.copy(concepto = "Cañas del viernes")

        assertEquals(original.id, editado.id)
        assertEquals(original.fechaMillis, editado.fechaMillis)
        assertEquals(setOf("a", "l"), editado.pulgaresArriba)
        assertEquals(setOf("p"), editado.pulgaresAbajo)
        assertEquals(1, editado.saldoPulgares)
    }

    @Test
    fun `un grupo donde solo yo estoy a cero no esta en paz`() {
        // La portada cantaba "Nadie debe nada a nadie" mirando solo mi saldo.
        val grupo = listOf(ruben, ana, luis)
        val gastos = listOf(
            // Ana paga algo que solo consumen Ana y Luis: yo quedo a cero.
            Gasto("1", "g", "Cena de dos", 3000, "a", 0L, reparto = Reparto.Escote(listOf("a", "l")))
        )
        val saldos = Cuentas.saldos(grupo, gastos)
        val mio = Cuentas.loMioDelPlan(Cuentas.planDePagos(saldos), "r")

        assertTrue("Yo estoy a cero", mio.enPaz)
        assertFalse("Pero el grupo no", saldos.all { it.enPaz })
    }
}
