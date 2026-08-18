package com.pulgares.app

import com.pulgares.app.data.red.ClienteNube
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
    fun `la base del cliente nunca acaba en pulgares`() {
        // La URL por defecto del primer dia acababa en /pulgares y el cliente
        // añade /pulgares/crear: las peticiones salian a /pulgares/pulgares/crear
        // y el 404 del gateway parecia que faltaba la infraestructura (cazado por
        // Rubén probando en su movil).
        val bueno = "https://api.example.com"
        assertEquals(bueno, ClienteNube.normaliza("https://api.example.com/pulgares"))
        assertEquals(bueno, ClienteNube.normaliza("https://api.example.com/pulgares/"))
        assertEquals(bueno, ClienteNube.normaliza("https://api.example.com/"))
        assertEquals(bueno, ClienteNube.normaliza("https://api.example.com"))
        assertEquals(bueno, ClienteNube.normaliza(" https://api.example.com/pulgares "))
    }

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
    fun `una lapida gana al gasto vivo si es mas nueva`() {
        // El caso que motiva las lapidas: A borra un gasto (lapida v2000) y B
        // todavia tiene la copia viva (v1000). Al fusionar, el borrado gana; si
        // se borrase la fila de verdad, B lo devolveria y el gasto resucitaria.
        val vivo = gasto("1", version = 1000)
        val lapida = gasto("1", version = 2000).copy(borrado = true)

        val a = Sincronizador.fusiona(listOf(lapida), listOf(vivo), Gasto::id, Gasto::version)
        assertTrue(a.single().borrado)

        val b = Sincronizador.fusiona(listOf(vivo), listOf(lapida), Gasto::id, Gasto::version)
        assertTrue(b.single().borrado)
    }

    @Test
    fun `el flag de borrado viaja en los dos sentidos`() {
        val paquete = Sincronizador.paqueteDeSubida(
            grupoId = "local",
            remotoId = "remoto",
            colegas = emptyList(),
            gastos = listOf(gasto("1").copy(borrado = true)),
            pagos = emptyList()
        )
        assertTrue(paquete.getJSONArray("gastos").getJSONObject(0).getBoolean("borrado"))

        val leido = Sincronizador.leeGrupo(
            JSONObject(
                """{"grupoId":"x","nombre":"N","emoji":"👥","codigo":"AAAAAA",
                    "gastos":[{"id":"1","concepto":"C","importeCentimos":100,
                    "pagadorId":"r","fechaMillis":1,"categoria":"BIRRAS",
                    "reparto":"escote:r","version":5,"borrado":true}]}"""
            ),
            "local"
        )
        assertTrue(leido.gastos.single().borrado)
    }

    @Test
    fun `las solicitudes de entrar llegan solo si soy el dueño`() {
        // Respuesta real del backend a /pulgares/grupo siendo el dueño.
        val json = JSONObject(
            """
            {
              "grupoId": "abc", "nombre": "Cañas", "emoji": "🍻", "codigo": "HGRND2",
              "version": 1, "soyElDueno": true, "miColegaId": "col-yo",
              "colegas": [{"id": "col-yo", "nombre": "Rubén", "avatar": null, "activo": true, "version": 1}],
              "colegasLibres": [],
              "solicitudes": [
                {"uid": "movil-ana", "nombre": "Ana", "avatar": "m1:2,5,4,8,3,0,0,0,0,1,0", "pedida": 123}
              ],
              "gastos": [], "pagos": []
            }
            """.trimIndent()
        )
        val remoto = Sincronizador.leeGrupo(json, "local")
        val solicitud = remoto.solicitudes.single()
        assertEquals("movil-ana", solicitud.uid)
        assertEquals("Ana", solicitud.nombre)
        assertEquals(123L, solicitud.pedidaMillis)
        assertTrue(solicitud.avatar!!.startsWith("m1:"))

        // Y si el backend no manda el campo (no soy el dueño), lista vacía.
        json.remove("solicitudes")
        assertEquals(emptyList<Any>(), Sincronizador.leeGrupo(json, "local").solicitudes)
    }

    @Test
    fun `los zumbidos llegan con remitente y contador`() {
        // Respuesta real del backend: Ana zumbó tres veces y se entrega UNA vez.
        val json = JSONObject(
            """
            {"grupoId":"x","nombre":"Cañas","emoji":"🍻","codigo":"AAAAAA",
             "zumbidos":[{"de":"col-ana","creado":1787070946889,"veces":3}]}
            """.trimIndent()
        )
        val remoto = Sincronizador.leeGrupo(json, "local")
        val zumbido = remoto.zumbidos.single()
        assertEquals("col-ana", zumbido.deColegaId)
        assertEquals(3, zumbido.veces)

        // Sin el campo (respuesta vieja o sin zumbidos), lista vacía y en paz.
        json.remove("zumbidos")
        assertEquals(emptyList<Any>(), Sincronizador.leeGrupo(json, "local").zumbidos)
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
