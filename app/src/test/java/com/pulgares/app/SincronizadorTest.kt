package com.pulgares.app

import com.pulgares.app.data.red.Sincronizador
import com.pulgares.app.domain.model.Categoria
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Pago
import com.pulgares.app.domain.model.Reparto
import com.pulgares.app.domain.settlement.Cuentas
import com.pulgares.app.domain.model.Colega
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SincronizadorTest {

    private fun gasto(
        id: String,
        importe: Long = 3000,
        version: Long = 0,
        concepto: String = "Cañas"
    ) = Gasto(
        id = id,
        grupoId = "g",
        concepto = concepto,
        importeCentimos = importe,
        pagadorId = "r",
        fechaMillis = 1_000,
        categoria = Categoria.BIRRAS,
        reparto = Reparto.Escote(listOf("r", "a")),
        version = version
    )

    @Test
    fun `la fusion es la union por id`() {
        val locales = listOf(gasto("1"), gasto("2"))
        val remotos = listOf(gasto("2"), gasto("3"))
        val juntos = Sincronizador.fusiona(locales, remotos, Gasto::id, Gasto::version)
        assertEquals(setOf("1", "2", "3"), juntos.map { it.id }.toSet())
    }

    @Test
    fun `gana la version mas alta, venga de donde venga`() {
        val miEdicion = gasto("1", importe = 6400, version = 3000, concepto = "Taxi ida y vuelta")
        val copiaVieja = gasto("1", importe = 3200, version = 1000, concepto = "Taxi")

        // Mi edición nueva no la pisa una copia vieja que llega de la nube.
        val a = Sincronizador.fusiona(listOf(miEdicion), listOf(copiaVieja), Gasto::id, Gasto::version)
        assertEquals(6400L, a.single().importeCentimos)

        // Y al revés: si la nube va por delante, se acepta lo de la nube.
        val b = Sincronizador.fusiona(listOf(copiaVieja), listOf(miEdicion), Gasto::id, Gasto::version)
        assertEquals(6400L, b.single().importeCentimos)
    }

    @Test
    fun `con la misma version se queda lo que ya habia`() {
        val local = gasto("1", concepto = "El de aquí", version = 500)
        val remoto = gasto("1", concepto = "El de allí", version = 500)
        val juntos = Sincronizador.fusiona(listOf(local), listOf(remoto), Gasto::id, Gasto::version)
        assertEquals("El de aquí", juntos.single().concepto)
    }

    @Test
    fun `fusionar no descuadra las cuentas`() {
        // Lo importante de todo esto: después de juntar dos móviles, los saldos
        // siguen sumando cero.
        val gente = listOf(Colega("r", "Rubén", soyYo = true), Colega("a", "Ana"))
        val locales = listOf(gasto("1", 3000), gasto("2", 1550))
        val remotos = listOf(gasto("2", 1550), gasto("3", 999))

        val gastos = Sincronizador.fusiona(locales, remotos, Gasto::id, Gasto::version)
        val saldos = Cuentas.saldos(gente, gastos)
        assertEquals(0L, saldos.sumOf { it.neto })
        assertTrue(gastos.all { it.cuadra })
    }

    @Test
    fun `los pagos se fusionan igual`() {
        val local = Pago("p1", "g", "a", "r", 1000, 0L, version = 10)
        val remoto = Pago("p2", "g", "l", "r", 500, 0L, version = 20)
        val juntos = Sincronizador.fusiona(listOf(local), listOf(remoto), Pago::id, Pago::version)
        assertEquals(2, juntos.size)
        assertEquals(1500L, juntos.sumOf { it.importeCentimos })
    }

    @Test
    fun `el paquete de subida lleva todo lo que el backend necesita`() {
        val paquete = Sincronizador.paqueteDeSubida(
            grupoId = "local-1",
            remotoId = "remoto-1",
            colegas = listOf(Colega("r", "Rubén", soyYo = true), Colega("a", "Ana", activo = false)),
            gastos = listOf(gasto("1").copy(pulgaresArriba = setOf("a"))),
            pagos = listOf(Pago("p1", "g", "a", "r", 1000, 5L, version = 7))
        )

        assertEquals("remoto-1", paquete.getString("grupoId"))
        assertEquals(2, paquete.getJSONArray("colegas").length())
        // El que se fue viaja con activo=false, para que la nube no lo resucite.
        assertEquals(false, paquete.getJSONArray("colegas").getJSONObject(1).getBoolean("activo"))

        val gastoJson = paquete.getJSONArray("gastos").getJSONObject(0)
        assertEquals("escote:r,a", gastoJson.getString("reparto"))
        assertEquals(1, gastoJson.getJSONArray("pulgaresArriba").length())
        assertEquals(7L, paquete.getJSONArray("pagos").getJSONObject(0).getLong("version"))
    }

    @Test
    fun `lee un grupo tal y como lo manda el backend`() {
        // Este JSON es una respuesta real de /pulgares/unirse, recortada.
        val json = JSONObject(
            """
            {
              "grupoId": "48e982c3cb53",
              "nombre": "Viaje a Lisboa",
              "emoji": "✈️",
              "codigo": "65KP7G",
              "version": 1787004583545,
              "soyElDueno": false,
              "miColegaId": "col-ana",
              "colegas": [
                {"id": "col-ana", "nombre": "Ana", "avatar": null, "activo": true, "version": 1},
                {"id": "col-luis", "nombre": "Luis", "avatar": null, "activo": true, "version": 1}
              ],
              "colegasLibres": ["col-luis"],
              "gastos": [
                {"id": "g-cena", "concepto": "Cena", "importeCentimos": 9360,
                 "pagadorId": "col-ana", "fechaMillis": 1786900000000, "categoria": "COMIDA",
                 "reparto": "escote:col-ana,col-luis", "nota": null,
                 "pulgaresArriba": ["col-luis"], "pulgaresAbajo": [], "version": 1000}
              ],
              "pagos": [
                {"id": "p-1", "deQuienId": "col-luis", "aQuienId": "col-ana",
                 "importeCentimos": 3120, "fechaMillis": 1786930000000, "nota": null, "version": 2000}
              ]
            }
            """.trimIndent()
        )

        val remoto = Sincronizador.leeGrupo(json, grupoIdLocal = "mi-grupo")

        assertEquals("48e982c3cb53", remoto.remotoId)
        assertEquals("Viaje a Lisboa", remoto.nombre)
        assertEquals("65KP7G", remoto.codigo)
        assertEquals(listOf("col-luis"), remoto.colegasLibres)
        // El colega que soy yo queda marcado, que es lo que usa toda la app.
        assertEquals("col-ana", remoto.colegas.first { it.soyYo }.id)

        val gasto = remoto.gastos.single()
        assertEquals("mi-grupo", gasto.grupoId)
        assertEquals(9360L, gasto.importeCentimos)
        assertEquals(Categoria.COMIDA, gasto.categoria)
        assertEquals(setOf("col-luis"), gasto.pulgaresArriba)
        assertTrue(gasto.cuadra)
        assertEquals(null, gasto.nota)

        assertEquals(3120L, remoto.pagos.single().importeCentimos)
        assertEquals(2000L, remoto.pagos.single().version)
    }

    @Test
    fun `un grupo sin gastos ni colegas no revienta al leerlo`() {
        val json = JSONObject("""{"grupoId":"x","nombre":"Nuevo","emoji":"👥","codigo":"AAAAAA"}""")
        val remoto = Sincronizador.leeGrupo(json, "local")
        assertEquals(emptyList<Any>(), remoto.gastos)
        assertEquals(emptyList<Any>(), remoto.colegas)
        assertEquals(emptyList<Any>(), remoto.colegasLibres)
        assertEquals(null, remoto.miColegaId)
    }
}
