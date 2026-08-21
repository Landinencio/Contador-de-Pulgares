package com.pulgares.app.data.red

import com.pulgares.app.data.local.RepartoTexto
import com.pulgares.app.domain.model.Categoria
import com.pulgares.app.domain.model.Colega
import com.pulgares.app.domain.model.Gasto
import com.pulgares.app.domain.model.Pago
import org.json.JSONArray
import org.json.JSONObject

/**
 * Traduce entre el modelo de la app y el JSON del backend, y fusiona lo local con
 * lo remoto.
 *
 * La fusión es fácil por diseño: gastos y pagos llevan su id desde que se crean y
 * son cosas que ocurrieron, no estado mutable, así que juntar dos móviles es la
 * unión por id. Lo único que necesita árbitro es lo que se edita, y para eso está
 * `version`: gana la más alta, y si empatan gana lo remoto (da igual, son iguales).
 */
object Sincronizador {

    /** El paquete que se manda al backend con lo de este móvil. */
    fun paqueteDeSubida(
        grupoId: String,
        remotoId: String,
        colegas: List<Colega>,
        gastos: List<Gasto>,
        pagos: List<Pago>
    ): JSONObject {
        val cuerpo = JSONObject().put("grupoId", remotoId)

        cuerpo.put("colegas", JSONArray().apply {
            colegas.forEach { colega ->
                put(
                    JSONObject()
                        .put("id", colega.id)
                        .put("nombre", colega.nombre)
                        .put("avatar", colega.avatar ?: JSONObject.NULL)
                        .put("activo", colega.activo)
                        .put("version", colega.version)
                )
            }
        })

        cuerpo.put("gastos", JSONArray().apply {
            gastos.forEach { gasto ->
                put(
                    JSONObject()
                        .put("id", gasto.id)
                        .put("concepto", gasto.concepto)
                        .put("importeCentimos", gasto.importeCentimos)
                        .put("pagadorId", gasto.pagadorId)
                        .put("fechaMillis", gasto.fechaMillis)
                        .put("categoria", gasto.categoria.name)
                        .put("reparto", RepartoTexto.serializa(gasto.reparto))
                        .put("nota", gasto.nota ?: JSONObject.NULL)
                        .put("pulgaresArriba", JSONArray(gasto.pulgaresArriba.toList()))
                        .put("pulgaresAbajo", JSONArray(gasto.pulgaresAbajo.toList()))
                        .put("version", gasto.version)
                        .put("borrado", gasto.borrado)
                )
            }
        })

        cuerpo.put("pagos", JSONArray().apply {
            pagos.forEach { pago ->
                put(
                    JSONObject()
                        .put("id", pago.id)
                        .put("deQuienId", pago.deQuienId)
                        .put("aQuienId", pago.aQuienId)
                        .put("importeCentimos", pago.importeCentimos)
                        .put("fechaMillis", pago.fechaMillis)
                        .put("nota", pago.nota ?: JSONObject.NULL)
                        .put("version", pago.version)
                        .put("borrado", pago.borrado)
                )
            }
        })

        return cuerpo
    }

    /** Un zumbido entregado: quién te sacude y cuántas veces acumuladas. */
    data class Zumbido(
        val deColegaId: String,
        val creadoMillis: Long,
        val veces: Int
    )

    /** Una propuesta de nombre esperando los votos del grupo. */
    data class VotacionNombre(
        val nombre: String,
        val emoji: String,
        val quien: String,
        val creadoMillis: Long,
        val aFavor: Int,
        val enContra: Int,
        val votantes: Int,
        val heVotado: Boolean,
        val esMia: Boolean
    )

    /**
     * Cómo acabó el último cambio de nombre. A diferencia de los zumbidos esto no
     * se consume al entregarlo: lo tienen que ver todos los móviles, y ninguno
     * sabe de los demás, así que cada uno se apunta en local el último que cantó.
     */
    data class AvisoNombre(
        /** "directo" (cambio gratis), "sinVotantes", "aprobada" o "rechazada". */
        val resultado: String,
        val nombre: String,
        val quien: String,
        val creadoMillis: Long
    )

    /** Una petición de entrar al grupo, esperando al dueño. */
    data class Solicitud(
        val uid: String,
        val nombre: String,
        val avatar: String?,
        val pedidaMillis: Long
    )

    /** Lo que devuelve el backend, ya en modelo de la app. */
    data class GrupoRemoto(
        val remotoId: String,
        val nombre: String,
        val emoji: String,
        val codigo: String,
        val version: Long,
        val soyElDueno: Boolean,
        val miColegaId: String?,
        val colegas: List<Colega>,
        val colegasLibres: List<String>,
        /** Solo llegan si soy el dueño: es quien las aprueba. */
        val solicitudes: List<Solicitud>,
        /** Zumbidos dirigidos a mí; el servidor los borra al entregarlos. */
        val zumbidos: List<Zumbido>,
        /** La votación del nombre en marcha, si hay alguna. */
        val votacion: VotacionNombre?,
        /** Cómo acabó la última, para cantarlo una vez. */
        val avisoNombre: AvisoNombre?,
        /** ¿Me queda mi cambio de nombre gratis? */
        val meQuedaCambioGratis: Boolean,
        val gastos: List<Gasto>,
        val pagos: List<Pago>
    )

    fun leeGrupo(json: JSONObject, grupoIdLocal: String): GrupoRemoto {
        val remotoId = json.getString("grupoId")
        val miColega = json.optString("miColegaId").takeIf { it.isNotBlank() && it != "null" }

        val colegas = json.optJSONArray("colegas").porCada { item ->
            Colega(
                id = item.getString("id"),
                nombre = item.optString("nombre"),
                avatar = item.optString("avatar").takeIf { it.isNotBlank() && it != "null" },
                soyYo = item.getString("id") == miColega,
                activo = item.optBoolean("activo", true),
                version = item.optLong("version")
            )
        }

        val gastos = json.optJSONArray("gastos").porCada { item ->
            Gasto(
                id = item.getString("id"),
                grupoId = grupoIdLocal,
                concepto = item.optString("concepto"),
                importeCentimos = item.optLong("importeCentimos"),
                pagadorId = item.optString("pagadorId"),
                fechaMillis = item.optLong("fechaMillis"),
                categoria = Categoria.porNombre(item.optString("categoria")),
                reparto = RepartoTexto.parse(item.optString("reparto")),
                nota = item.optString("nota").takeIf { it.isNotBlank() && it != "null" },
                pulgaresArriba = item.optJSONArray("pulgaresArriba").aConjunto(),
                pulgaresAbajo = item.optJSONArray("pulgaresAbajo").aConjunto(),
                version = item.optLong("version"),
                borrado = item.optBoolean("borrado", false)
            )
        }.filterNot { it.id.isBlank() }

        val pagos = json.optJSONArray("pagos").porCada { item ->
            Pago(
                id = item.getString("id"),
                grupoId = grupoIdLocal,
                deQuienId = item.optString("deQuienId"),
                aQuienId = item.optString("aQuienId"),
                importeCentimos = item.optLong("importeCentimos"),
                fechaMillis = item.optLong("fechaMillis"),
                nota = item.optString("nota").takeIf { it.isNotBlank() && it != "null" },
                version = item.optLong("version"),
                borrado = item.optBoolean("borrado", false)
            )
        }

        return GrupoRemoto(
            remotoId = remotoId,
            nombre = json.optString("nombre"),
            emoji = json.optString("emoji").ifBlank { "👥" },
            codigo = json.optString("codigo"),
            version = json.optLong("version"),
            soyElDueno = json.optBoolean("soyElDueno", false),
            miColegaId = miColega,
            colegas = colegas,
            colegasLibres = json.optJSONArray("colegasLibres").aConjunto().toList(),
            solicitudes = json.optJSONArray("solicitudes").porCada { item ->
                Solicitud(
                    uid = item.optString("uid"),
                    nombre = item.optString("nombre"),
                    avatar = item.optString("avatar").takeIf { it.isNotBlank() && it != "null" },
                    pedidaMillis = item.optLong("pedida")
                )
            }.filterNot { it.uid.isBlank() },
            zumbidos = json.optJSONArray("zumbidos").porCada { item ->
                Zumbido(
                    deColegaId = item.optString("de"),
                    creadoMillis = item.optLong("creado"),
                    veces = item.optInt("veces", 1)
                )
            }.filterNot { it.deColegaId.isBlank() },
            votacion = json.optJSONObject("votacionNombre")?.let { voto ->
                VotacionNombre(
                    nombre = voto.optString("nombre"),
                    emoji = voto.optString("emoji"),
                    quien = voto.optString("quien").ifBlank { "Alguien" },
                    creadoMillis = voto.optLong("creado"),
                    aFavor = voto.optInt("aFavor"),
                    enContra = voto.optInt("enContra"),
                    votantes = voto.optInt("votantes"),
                    heVotado = voto.optBoolean("heVotado", false),
                    esMia = voto.optBoolean("esMia", false)
                )
            },
            avisoNombre = json.optJSONObject("avisoNombre")?.let { aviso ->
                AvisoNombre(
                    resultado = aviso.optString("resultado"),
                    nombre = aviso.optString("nombre"),
                    quien = aviso.optString("quien").ifBlank { "Alguien" },
                    creadoMillis = aviso.optLong("creado")
                )
            },
            meQuedaCambioGratis = json.optBoolean("meQuedaCambioGratis", true),
            gastos = gastos,
            pagos = pagos
        )
    }

    /**
     * Une dos listas por id quedándose con la versión más alta. Es toda la
     * política de conflictos que hace falta: los gastos son hechos, no estado
     * compartido, y el único caso de choque real es editar el mismo gasto en dos
     * móviles a la vez.
     */
    fun <T> fusiona(
        locales: List<T>,
        remotos: List<T>,
        id: (T) -> String,
        version: (T) -> Long
    ): List<T> {
        val porId = locales.associateByTo(LinkedHashMap(), id)
        remotos.forEach { remoto ->
            val local = porId[id(remoto)]
            if (local == null || version(remoto) > version(local)) {
                porId[id(remoto)] = remoto
            }
        }
        return porId.values.toList()
    }
}

private inline fun <T> JSONArray?.porCada(bloque: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    val salida = ArrayList<T>(length())
    for (i in 0 until length()) {
        val item = optJSONObject(i) ?: continue
        salida += bloque(item)
    }
    return salida
}

private fun JSONArray?.aConjunto(): Set<String> {
    if (this == null) return emptySet()
    val salida = LinkedHashSet<String>(length())
    for (i in 0 until length()) {
        optString(i).takeIf { it.isNotBlank() }?.let { salida += it }
    }
    return salida
}
