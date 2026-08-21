package com.pulgares.app.domain

import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Pago
import com.pulgares.app.domain.model.Reparto
import com.pulgares.app.domain.settlement.Cuentas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CuentasTest {

    private val ruben = Colega("r", "Rubén", soyYo = true)
    private val ana = Colega("a", "Ana")
    private val luis = Colega("l", "Luis")
    private val cuadrilla = listOf(ruben, ana, luis)

    private fun gasto(
        id: String,
        importe: Long,
        pagador: String,
        entre: List<String> = listOf("r", "a", "l")
    ) = Gasto(
        id = id,
        grupoId = "g",
        concepto = "Cosas",
        importeCentimos = importe,
        pagadorId = pagador,
        fechaMillis = 0L,
        reparto = Reparto.Escote(entre)
    )

    @Test
    fun `los netos siempre suman cero`() {
        val gastos = listOf(
            gasto("1", 3000, "r"),
            gasto("2", 1550, "a"),
            gasto("3", 899, "l"),
            gasto("4", 1000, "r", entre = listOf("r", "a"))
        )
        val saldos = Cuentas.saldos(cuadrilla, gastos)
        assertEquals(0L, saldos.sumOf { it.neto })
    }

    @Test
    fun `quien invita a todos queda como acreedor`() {
        // Rubén paga 30 € de una cena de tres: le deben 20 €.
        val saldos = Cuentas.saldos(cuadrilla, listOf(gasto("1", 3000, "r")))
        val mio = saldos.first { it.colegaId == "r" }
        assertEquals(3000L, mio.pagadoCentimos)
        assertEquals(1000L, mio.debidoCentimos)
        assertEquals(2000L, mio.neto)
        assertTrue(mio.esAcreedor)

        val deAna = saldos.first { it.colegaId == "a" }
        assertEquals(-1000L, deAna.neto)
        assertTrue(deAna.esDeudor)
    }

    @Test
    fun `un gasto que no incluye al pagador se le debe entero`() {
        // Rubén paga el regalo de Ana y Luis: no participa del reparto.
        val gastoRegalo = gasto("1", 4000, "r", entre = listOf("a", "l"))
        val saldos = Cuentas.saldos(cuadrilla, listOf(gastoRegalo))
        assertEquals(4000L, saldos.first { it.colegaId == "r" }.neto)
        assertEquals(-2000L, saldos.first { it.colegaId == "a" }.neto)
        assertEquals(-2000L, saldos.first { it.colegaId == "l" }.neto)
    }

    @Test
    fun `un bizum salda deuda y no cuenta como gasto`() {
        val gastos = listOf(gasto("1", 3000, "r"))
        val pagos = listOf(Pago("p1", "g", "a", "r", 1000, 0L))

        val saldos = Cuentas.saldos(cuadrilla, gastos, pagos)
        assertEquals(0L, saldos.first { it.colegaId == "a" }.neto)
        assertEquals(1000L, saldos.first { it.colegaId == "r" }.neto)
        assertEquals(0L, saldos.sumOf { it.neto })
        // El total gastado del grupo no cambia por mover dinero.
        assertEquals(3000L, Cuentas.totalGastado(gastos))
    }

    @Test
    fun `el plan de pagos deja a todo el mundo a cero`() {
        val gastos = listOf(
            gasto("1", 3000, "r"),
            gasto("2", 1550, "a"),
            gasto("3", 899, "l"),
            gasto("4", 2500, "r", entre = listOf("a", "l"))
        )
        val saldos = Cuentas.saldos(cuadrilla, gastos)
        val plan = Cuentas.planDePagos(saldos)

        // Aplicar el plan a los netos debe dejarlos todos en cero.
        val netos = saldos.associate { it.colegaId to it.neto }.toMutableMap()
        for (t in plan) {
            netos[t.deQuienId] = (netos[t.deQuienId] ?: 0L) + t.importeCentimos
            netos[t.aQuienId] = (netos[t.aQuienId] ?: 0L) - t.importeCentimos
        }
        assertTrue("Alguien queda descuadrado: $netos", netos.values.all { it == 0L })
    }

    @Test
    fun `el plan usa como maximo N-1 transferencias`() {
        val gente = (1..6).map { Colega("c$it", "Colega $it") }
        val gastos = listOf(
            Gasto("1", "g", "Hotel", 24000, "c1", 0L, reparto = Reparto.Escote(gente.map { it.id })),
            Gasto("2", "g", "Cena", 12000, "c2", 0L, reparto = Reparto.Escote(gente.map { it.id })),
            Gasto("3", "g", "Furgo", 9000, "c3", 0L, reparto = Reparto.Escote(gente.map { it.id }))
        )
        val plan = Cuentas.planDePagos(Cuentas.saldos(gente, gastos))
        assertTrue("Demasiados bizums: ${plan.size}", plan.size <= gente.size - 1)
    }

    @Test
    fun `caso de libro tres deudores y un acreedor`() {
        // Uno paga 90 € de una cena de cuatro: tres bizums de 22,50 €.
        val gente = listOf(ruben, ana, luis, Colega("p", "Pepe"))
        val gastos = listOf(
            Gasto("1", "g", "Cena", 9000, "r", 0L, reparto = Reparto.Escote(gente.map { it.id }))
        )
        val plan = Cuentas.planDePagos(Cuentas.saldos(gente, gastos))
        assertEquals(3, plan.size)
        assertTrue(plan.all { it.aQuienId == "r" })
        assertTrue(plan.all { it.importeCentimos == 2250L })
    }

    @Test
    fun `grupo saldado no genera plan`() {
        val gastos = listOf(
            Gasto("1", "g", "A", 1000, "r", 0L, reparto = Reparto.Escote(listOf("r", "a"))),
            Gasto("2", "g", "B", 1000, "a", 0L, reparto = Reparto.Escote(listOf("r", "a")))
        )
        val saldos = Cuentas.saldos(listOf(ruben, ana), gastos)
        assertTrue(saldos.all { it.enPaz })
        assertTrue(Cuentas.planDePagos(saldos).isEmpty())
    }

    @Test
    fun `reparto por partes el que repitio paga mas`() {
        val gastoPorPartes = Gasto(
            id = "1",
            grupoId = "g",
            concepto = "Picoteo",
            importeCentimos = 3000,
            pagadorId = "r",
            fechaMillis = 0L,
            reparto = Reparto.PorPartes(mapOf("r" to 1, "a" to 2, "l" to 1))
        )
        val deudas = gastoPorPartes.deudas()
        assertEquals(3000L, deudas.values.sum())
        assertEquals(1500L, deudas["a"])
        assertEquals(750L, deudas["r"])
        assertEquals(750L, deudas["l"])
    }

    @Test
    fun `reparto exacto respeta lo que puso cada uno`() {
        val gastoExacto = Gasto(
            id = "1",
            grupoId = "g",
            concepto = "Supermercado",
            importeCentimos = 4550,
            pagadorId = "r",
            fechaMillis = 0L,
            reparto = Reparto.Exacto(mapOf("r" to 2000L, "a" to 1550L, "l" to 1000L))
        )
        val saldos = Cuentas.saldos(cuadrilla, listOf(gastoExacto))
        assertEquals(2550L, saldos.first { it.colegaId == "r" }.neto)
        assertEquals(-1550L, saldos.first { it.colegaId == "a" }.neto)
        assertEquals(0L, saldos.sumOf { it.neto })
    }

    @Test
    fun `lo mio del plan es el neto, no la deuda bilateral`() {
        // Ana paga una cena de 30 € (10 € cada uno) y Rubén paga 6 € que solo
        // consume Luis. En bruto Rubén debe 10 € a Ana y Luis le debe 6 € a él,
        // pero el plan lo simplifica: Rubén solo mueve los 4 € de diferencia.
        val gastos = listOf(
            gasto("1", 3000, "a"),
            gasto("2", 600, "r", entre = listOf("l"))
        )
        val plan = Cuentas.planDePagos(Cuentas.saldos(cuadrilla, gastos))
        val mio = Cuentas.loMioDelPlan(plan, "r")
        assertEquals(400L, mio.deboCentimos)
        assertEquals(0L, mio.meDebenCentimos)
        assertEquals(-400L, mio.neto)
    }

    @Test
    fun `la simplificacion rompe el triangulo de deudas`() {
        // Ana paga lo de Rubén y Rubén paga lo de Luis por el mismo importe:
        // en vez de dos bizums en cadena, Luis le paga directamente a Ana.
        val gastos = listOf(
            gasto("1", 1000, "a", entre = listOf("r")),
            gasto("2", 1000, "r", entre = listOf("l"))
        )
        val plan = Cuentas.planDePagos(Cuentas.saldos(cuadrilla, gastos))
        assertEquals(1, plan.size)
        assertEquals("l", plan.first().deQuienId)
        assertEquals("a", plan.first().aQuienId)
        assertEquals(1000L, plan.first().importeCentimos)
        // Rubén queda al margen: ni pone ni recibe.
        assertTrue(Cuentas.loMioDelPlan(plan, "r").enPaz)
    }


    @Test
    fun `un reparto exacto que no suma el importe no cuadra`() {
        // La pantalla de gasto no deja guardar esto, pero el modelo tiene que
        // saber detectarlo: si colara, los saldos del grupo dejarian de sumar cero.
        val descuadrado = Gasto(
            id = "1",
            grupoId = "g",
            concepto = "Mal repartido",
            importeCentimos = 3000,
            pagadorId = "r",
            fechaMillis = 0L,
            reparto = Reparto.Exacto(mapOf("r" to 1000L, "a" to 500L))
        )
        assertFalse(descuadrado.cuadra)

        val bien = descuadrado.copy(
            reparto = Reparto.Exacto(mapOf("r" to 2000L, "a" to 1000L))
        )
        assertTrue(bien.cuadra)
    }

    @Test
    fun `los repartos a escote y por partes cuadran siempre`() {
        // Importes elegidos para que la division nunca sea exacta.
        listOf(1L, 7L, 99L, 1000L, 3333L, 99_999L).forEach { importe ->
            listOf(1, 2, 3, 5, 7).forEach { cuantos ->
                val ids = (1..cuantos).map { "c$it" }
                val escote = Gasto(
                    id = "e-$importe-$cuantos",
                    grupoId = "g",
                    concepto = "",
                    importeCentimos = importe,
                    pagadorId = "c1",
                    fechaMillis = 0L,
                    reparto = Reparto.Escote(ids)
                )
                assertTrue("Escote descuadrado: $importe entre $cuantos", escote.cuadra)

                val porPartes = escote.copy(
                    reparto = Reparto.PorPartes(ids.mapIndexed { i, id -> id to (i + 1) }.toMap())
                )
                assertTrue("Partes descuadradas: $importe entre $cuantos", porPartes.cuadra)
            }
        }
    }

    @Test
    fun `un gasto de un centimo entre tres sigue cuadrando`() {
        val calderilla = Gasto(
            id = "1",
            grupoId = "g",
            concepto = "El chicle",
            importeCentimos = 1,
            pagadorId = "r",
            fechaMillis = 0L,
            reparto = Reparto.Escote(listOf("r", "a", "l"))
        )
        assertEquals(1L, calderilla.deudas().values.sum())
        // Uno paga el centimo y los otros dos, nada.
        assertEquals(setOf(0L, 1L), calderilla.deudas().values.toSet())
        assertEquals(0L, Cuentas.saldos(cuadrilla, listOf(calderilla)).sumOf { it.neto })
    }

    @Test
    fun `un grupo de una sola persona no genera plan`() {
        val soloYo = listOf(ruben)
        val gastos = listOf(
            Gasto("1", "g", "Mi cena", 2000, "r", 0L, reparto = Reparto.Escote(listOf("r")))
        )
        val saldos = Cuentas.saldos(soloYo, gastos)
        assertTrue(saldos.all { it.enPaz })
        assertTrue(Cuentas.planDePagos(saldos).isEmpty())
    }
    @Test
    fun `los pulgares cuentan arriba menos abajo`() {
        val votado = gasto("1", 1000, "r").copy(
            pulgaresArriba = setOf("a", "l"),
            pulgaresAbajo = setOf("p")
        )
        assertEquals(1, votado.saldoPulgares)
    }

    // ---- a quien le toca poner la siguiente ----

    private fun puesto(
        id: String,
        importe: Long,
        pagador: String,
        cuando: Long,
        borrado: Boolean = false
    ) = gasto(id, importe, pagador).copy(fechaMillis = cuando, borrado = borrado)

    @Test
    fun `la siguiente la paga el que menos ha puesto`() {
        // Rubén 50, Ana 10, Luis 30 -> le toca a Ana.
        val gastos = listOf(
            puesto("1", 5_000, "r", cuando = 100),
            puesto("2", 1_000, "a", cuando = 200),
            puesto("3", 3_000, "l", cuando = 300)
        )
        val turno = Cuentas.quienPagaLaSiguiente(cuadrilla, gastos)!!
        assertEquals("a", turno.colegaId)
        assertEquals(1_000L, turno.puestoCentimos)

        // Quien no ha puesto NADA va primero, aunque no salga en ningun gasto.
        val conCuarto = cuadrilla + Colega("d", "Dani")
        assertEquals("d", Cuentas.quienPagaLaSiguiente(conCuarto, gastos)!!.colegaId)

        // Empate a dinero: paga el que lleva mas tiempo sin poner.
        val empate = listOf(
            puesto("1", 2_000, "r", cuando = 900),
            puesto("2", 2_000, "a", cuando = 100),
            puesto("3", 9_000, "l", cuando = 500)
        )
        assertEquals("a", Cuentas.quienPagaLaSiguiente(cuadrilla, empate)!!.colegaId)

        // Un gasto borrado no cuenta: si contara, bastaria con borrar el propio
        // gasto para librarse de la ronda... al contrario, quien lo borro pasa a
        // ser el que menos ha puesto.
        val conLapida = listOf(
            puesto("1", 5_000, "r", cuando = 100),
            puesto("2", 9_000, "a", cuando = 200, borrado = true),
            puesto("3", 3_000, "l", cuando = 300)
        )
        assertEquals("a", Cuentas.quienPagaLaSiguiente(cuadrilla, conLapida)!!.colegaId)

        // Sin gastos, o con menos de dos, no hay ronda que repartir.
        assertNull(Cuentas.quienPagaLaSiguiente(cuadrilla, emptyList()))
        assertNull(Cuentas.quienPagaLaSiguiente(listOf(ruben), gastos))

        // Determinista: el aviso es para TODOS, asi que tiene que salir el mismo
        // nombre en todos los moviles llegue la lista como llegue.
        assertEquals(
            "a",
            Cuentas.quienPagaLaSiguiente(cuadrilla.reversed(), gastos.reversed())!!.colegaId
        )
    }
}
